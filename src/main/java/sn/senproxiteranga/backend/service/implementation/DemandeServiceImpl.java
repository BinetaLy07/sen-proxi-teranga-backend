package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.senproxiteranga.backend.domain.Client;
import sn.senproxiteranga.backend.domain.Demande;
import sn.senproxiteranga.backend.domain.Professionnel;
import sn.senproxiteranga.backend.domain.ServiceProfessionnel;
import sn.senproxiteranga.backend.domain.Zone;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.domain.enums.StatutDemande;
import sn.senproxiteranga.backend.domain.enums.StatutVerification;
import sn.senproxiteranga.backend.dto.AccepterDemandeRequest;
import sn.senproxiteranga.backend.dto.DemandeRequest;
import sn.senproxiteranga.backend.dto.DemandeResponse;
import sn.senproxiteranga.backend.dto.MotifRequest;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.DemandeMapper;
import sn.senproxiteranga.backend.repository.ClientRepository;
import sn.senproxiteranga.backend.repository.DemandeRepository;
import sn.senproxiteranga.backend.repository.ProfessionnelRepository;
import sn.senproxiteranga.backend.repository.ServiceProfessionnelRepository;
import sn.senproxiteranga.backend.repository.ZoneRepository;
import sn.senproxiteranga.backend.service.DemandeService;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class DemandeServiceImpl implements DemandeService {

    // Délai de réponse du professionnel (règle des 48 h, à valider avec l'encadreur)
    private static final int DELAI_REPONSE_HEURES = 48;

    // Statuts où une annulation est encore possible (avant "En cours")
    private static final Set<StatutDemande> STATUTS_ANNULABLES = EnumSet.of(
            StatutDemande.CREEE,
            StatutDemande.ACCEPTEE,
            StatutDemande.DEVIS_ENVOYE,
            StatutDemande.DEVIS_ACCEPTE,
            StatutDemande.PLANIFIEE
    );

    private final DemandeRepository demandeRepository;
    private final ClientRepository clientRepository;
    private final ProfessionnelRepository professionnelRepository;
    private final ServiceProfessionnelRepository serviceRepository;
    private final ZoneRepository zoneRepository;
    private final DemandeMapper demandeMapper;

    // =============== CÔTÉ CLIENT ===============

    @Override
    public DemandeResponse creer(Long clientId, DemandeRequest request) {
        Client client = chercherClientActif(clientId);                     // Règle 1
        ServiceProfessionnel service = chercherServiceDisponible(request.serviceId()); // Règle 2

        Demande demande = demandeMapper.toEntity(request);
        demande.setClient(client);
        demande.setService(service);
        demande.setProfessionnel(service.getProfessionnel());
        demande.setZone(chercherZone(request.zoneId()));
        demande.setStatut(StatutDemande.CREEE);                             // Règle 3
        demande.setDateExpiration(LocalDateTime.now().plusHours(DELAI_REPONSE_HEURES));

        return demandeMapper.toResponse(demandeRepository.save(demande));
    }

    @Override
    public DemandeResponse modifier(Long clientId, Long demandeId, DemandeRequest request) {
        Demande demande = chercherDemandeDuClient(clientId, demandeId);
        if (demande.getStatut() != StatutDemande.CREEE) {                    // Règle 4
            throw new BusinessException("La demande ne peut plus être modifiée : elle a déjà été prise en charge");
        }
        if (!demande.getService().getId().equals(request.serviceId())) {
            throw new BusinessException("Le service ne peut pas être changé : annulez et créez une nouvelle demande");
        }
        demandeMapper.updateEntity(demande, request);
        demande.setZone(chercherZone(request.zoneId()));
        return demandeMapper.toResponse(demandeRepository.save(demande));
    }

    @Override
    public DemandeResponse annulerParClient(Long clientId, Long demandeId, MotifRequest request) {
        Demande demande = chercherDemandeDuClient(clientId, demandeId);
        return annuler(demande, request.motif());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeResponse> listerParClient(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client introuvable : " + clientId);
        }
        return demandeRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(demandeMapper::toResponse)
                .toList();
    }

    // =============== CÔTÉ PROFESSIONNEL ===============

    @Override
    public DemandeResponse accepter(Long professionnelId, Long demandeId, AccepterDemandeRequest request) {
        Demande demande = chercherDemandeDuPro(professionnelId, demandeId);
        verifierEnAttenteDeReponse(demande);                                // Règle 5

        // Règle 8 : frais de visite (c'est le professionnel qui décide)
        Double frais = (request != null) ? request.fraisVisite() : null;
        if (demande.isVisiteDemandee()) {
            // Visite demandée : montant indiqué par le pro, ou 0 (gratuite) s'il n'indique rien
            demande.setFraisVisite(frais != null ? frais : 0.0);
        } else if (frais != null && frais > 0) {
            // Pas de visite demandée : le pro ne peut pas faire payer une visite
            throw new BusinessException("Le client n'a pas demandé de visite : aucun frais de visite ne peut être fixé");
        }

        demande.setStatut(StatutDemande.ACCEPTEE);
        return demandeMapper.toResponse(demandeRepository.save(demande));
    }

    @Override
    public DemandeResponse refuser(Long professionnelId, Long demandeId, MotifRequest request) {
        Demande demande = chercherDemandeDuPro(professionnelId, demandeId);
        verifierEnAttenteDeReponse(demande);                                // Règle 5
        demande.setStatut(StatutDemande.REFUSEE);
        demande.setMotifRefus(request.motif().trim());
        return demandeMapper.toResponse(demandeRepository.save(demande));
    }

    @Override
    public DemandeResponse annulerParProfessionnel(Long professionnelId, Long demandeId, MotifRequest request) {
        Demande demande = chercherDemandeDuPro(professionnelId, demandeId);
        return annuler(demande, request.motif());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeResponse> listerParProfessionnel(Long professionnelId, StatutDemande statut) {
        if (!professionnelRepository.existsById(professionnelId)) {
            throw new ResourceNotFoundException("Professionnel introuvable : " + professionnelId);
        }
        List<Demande> demandes = (statut == null)
                ? demandeRepository.findByProfessionnelIdOrderByCreatedAtDesc(professionnelId)
                : demandeRepository.findByProfessionnelIdAndStatutOrderByCreatedAtDesc(professionnelId, statut);
        return demandes.stream()
                .map(demandeMapper::toResponse)
                .toList();
    }

    // =============== COMMUN ===============

    @Override
    @Transactional(readOnly = true)
    public DemandeResponse trouverParId(Long demandeId) {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : " + demandeId));
        return demandeMapper.toResponse(demande);
    }

    // =============== MÉTHODES INTERNES ===============

    // Règle 6 : annulation possible seulement avant "En cours"
    private DemandeResponse annuler(Demande demande, String motif) {
        if (!STATUTS_ANNULABLES.contains(demande.getStatut())) {
            throw new BusinessException("La demande ne peut plus être annulée (statut : " + demande.getStatut() + ")");
        }
        demande.setStatut(StatutDemande.ANNULEE);
        demande.setMotifAnnulation(motif.trim());
        return demandeMapper.toResponse(demandeRepository.save(demande));
    }

    // Règle 5 : le pro ne répond qu'à une demande CREEE, et dans le délai
    private void verifierEnAttenteDeReponse(Demande demande) {
        if (demande.getStatut() != StatutDemande.CREEE) {
            throw new BusinessException("Cette demande a déjà été traitée (statut : " + demande.getStatut() + ")");
        }
        if (demande.getDateExpiration() != null && LocalDateTime.now().isAfter(demande.getDateExpiration())) {
            throw new BusinessException("Le délai de réponse de 48 h est dépassé");
        }
    }

    // Règle 1 : le client existe et n'est pas suspendu
    private Client chercherClientActif(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable : " + clientId));
        if (client.getStatutCompte() == StatutCompte.SUSPENDU) {
            throw new BusinessException("Votre compte est suspendu : vous ne pouvez pas créer de demande");
        }
        return client;
    }

    // Règle 2 : service actif, d'un professionnel actif ET vérifié
    private ServiceProfessionnel chercherServiceDisponible(Long serviceId) {
        ServiceProfessionnel service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service introuvable : " + serviceId));
        if (!service.isActif()) {
            throw new BusinessException("Ce service n'est pas disponible actuellement");
        }
        Professionnel pro = service.getProfessionnel();
        if (pro.getStatutCompte() == StatutCompte.SUSPENDU) {
            throw new BusinessException("Ce professionnel n'est pas disponible");
        }
        if (pro.getStatutVerification() != StatutVerification.VALIDE) {
            throw new BusinessException("Ce professionnel n'a pas encore été vérifié par l'administrateur");
        }
        return service;
    }

    private Zone chercherZone(Long zoneId) {
        if (zoneId == null) {
            return null;
        }
        return zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone introuvable : " + zoneId));
    }

    // Règle 7 : chacun ne touche qu'à ses propres demandes
    private Demande chercherDemandeDuClient(Long clientId, Long demandeId) {
        return demandeRepository.findByIdAndClientId(demandeId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande " + demandeId + " introuvable pour ce client"));
    }

    private Demande chercherDemandeDuPro(Long professionnelId, Long demandeId) {
        return demandeRepository.findByIdAndProfessionnelId(demandeId, professionnelId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande " + demandeId + " introuvable pour ce professionnel"));
    }
}