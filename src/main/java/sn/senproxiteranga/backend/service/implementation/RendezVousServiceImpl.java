package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.senproxiteranga.backend.domain.Demande;
import sn.senproxiteranga.backend.domain.RendezVous;
import sn.senproxiteranga.backend.domain.enums.StatutDemande;
import sn.senproxiteranga.backend.domain.enums.StatutRendezVous;
import sn.senproxiteranga.backend.dto.RendezVousRequest;
import sn.senproxiteranga.backend.dto.RendezVousResponse;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.RendezVousMapper;
import sn.senproxiteranga.backend.repository.DemandeRepository;
import sn.senproxiteranga.backend.repository.RendezVousRepository;
import sn.senproxiteranga.backend.service.RendezVousService;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class RendezVousServiceImpl implements RendezVousService {

    // Un rendez-vous "actif" attend une réponse (PROPOSE) ou est confirmé (ACCEPTE)
    private static final Set<StatutRendezVous> STATUTS_ACTIFS = EnumSet.of(
            StatutRendezVous.PROPOSE,
            StatutRendezVous.ACCEPTE
    );

    // Taille maximale de la colonne "motif"
    private static final int TAILLE_MAX_MOTIF = 500;

    private final RendezVousRepository rendezVousRepository;
    private final DemandeRepository demandeRepository;
    private final RendezVousMapper rendezVousMapper;

    // =====================================================================
    //                    ACTIONS DU PROFESSIONNEL
    // =====================================================================

    @Override
    public RendezVousResponse proposer(Long professionnelId, Long demandeId, RendezVousRequest request) {
        Demande demande = chercherDemandeDuPro(professionnelId, demandeId);

        // Règle 1 : une date seulement après l'acceptation du devis
        if (demande.getStatut() != StatutDemande.DEVIS_ACCEPTE) {
            throw new BusinessException(
                    "Une date ne peut être proposée qu'après l'acceptation du devis (statut actuel : "
                            + demande.getStatut() + ")");
        }

        // Règle 2 : un seul rendez-vous actif à la fois
        if (rendezVousRepository.existsByDemandeIdAndStatutIn(demandeId, STATUTS_ACTIFS)) {
            throw new BusinessException(
                    "Une date est déjà en attente de réponse pour cette demande");
        }

        RendezVous rendezVous = rendezVousMapper.toEntity(demande, request);
        return rendezVousMapper.toResponse(rendezVousRepository.save(rendezVous));
    }

    @Override
    public RendezVousResponse reporterParProfessionnel(Long professionnelId, Long demandeId, String motif) {
        Demande demande = chercherDemandeDuPro(professionnelId, demandeId);
        return reporter(demande, "Report demandé par le professionnel : " + motif.trim());
    }

    @Override
    public RendezVousResponse commencer(Long professionnelId, Long demandeId) {
        Demande demande = chercherDemandeDuPro(professionnelId, demandeId);

        // Règle 5 : on ne commence qu'un rendez-vous planifié
        if (demande.getStatut() != StatutDemande.PLANIFIEE) {
            throw new BusinessException(
                    "Les travaux ne peuvent commencer que pour un rendez-vous planifié (statut actuel : "
                            + demande.getStatut() + ")");
        }

        RendezVous rendezVous = chercherActuel(demandeId);
        rendezVous.setDateDebutTravaux(LocalDateTime.now());
        demande.setStatut(StatutDemande.EN_COURS);

        return rendezVousMapper.toResponse(rendezVous);
    }

    @Override
    public RendezVousResponse terminer(Long professionnelId, Long demandeId) {
        Demande demande = chercherDemandeDuPro(professionnelId, demandeId);

        // Règle 6 : on ne termine que des travaux en cours
        if (demande.getStatut() != StatutDemande.EN_COURS) {
            throw new BusinessException(
                    "Les travaux ne peuvent être terminés que s'ils sont en cours (statut actuel : "
                            + demande.getStatut() + ")");
        }

        RendezVous rendezVous = chercherActuel(demandeId);
        rendezVous.setDateFinTravaux(LocalDateTime.now());
        demande.setStatut(StatutDemande.TERMINEE);

        return rendezVousMapper.toResponse(rendezVous);
    }

    // =====================================================================
    //                        ACTIONS DU CLIENT
    // =====================================================================

    @Override
    public RendezVousResponse accepter(Long clientId, Long demandeId) {
        Demande demande = chercherDemandeDuClient(clientId, demandeId);
        RendezVous rendezVous = chercherActuel(demandeId);
        verifierEnAttenteDeReponse(rendezVous);                            // Règle 3

        // Règle 4 : on n'accepte pas une date déjà passée
        if (rendezVous.getDateHeure().isBefore(LocalDateTime.now())) {
            throw new BusinessException(
                    "Cette date est déjà passée : refusez-la pour que le professionnel en propose une autre");
        }

        rendezVous.setStatut(StatutRendezVous.ACCEPTE);
        demande.setStatut(StatutDemande.PLANIFIEE);

        return rendezVousMapper.toResponse(rendezVous);
    }

    @Override
    public RendezVousResponse refuser(Long clientId, Long demandeId, String motif) {
        chercherDemandeDuClient(clientId, demandeId);
        RendezVous rendezVous = chercherActuel(demandeId);
        verifierEnAttenteDeReponse(rendezVous);                            // Règle 3

        // La demande reste DEVIS_ACCEPTE : le pro proposera une autre date
        rendezVous.setStatut(StatutRendezVous.REFUSE);
        rendezVous.setMotif(limiter(motif.trim()));

        return rendezVousMapper.toResponse(rendezVous);
    }

    @Override
    public RendezVousResponse reporterParClient(Long clientId, Long demandeId, String motif) {
        Demande demande = chercherDemandeDuClient(clientId, demandeId);
        return reporter(demande, "Report demandé par le client : " + motif.trim());
    }

    @Override
    public RendezVousResponse confirmerFinTravaux(Long clientId, Long demandeId) {
        Demande demande = chercherDemandeDuClient(clientId, demandeId);

        // Règle 7 : le client confirme seulement des travaux terminés
        if (demande.getStatut() != StatutDemande.TERMINEE) {
            throw new BusinessException(
                    "Vous ne pouvez confirmer que des travaux terminés (statut actuel : "
                            + demande.getStatut() + ")");
        }

        demande.setStatut(StatutDemande.CONFIRMEE);
        return rendezVousMapper.toResponse(chercherActuel(demandeId));
    }

    // =====================================================================
    //                          CONSULTATION
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public RendezVousResponse trouverActuel(Long demandeId) {
        verifierDemandeExiste(demandeId);
        return rendezVousMapper.toResponse(chercherActuel(demandeId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousResponse> historique(Long demandeId) {
        verifierDemandeExiste(demandeId);
        return rendezVousRepository.findByDemandeIdOrderByIdAsc(demandeId).stream()
                .map(rendezVousMapper::toResponse)
                .toList();
    }

    // =====================================================================
    //                     MÉTHODES INTERNES (règles)
    // =====================================================================

    /**
     * Règle 8 : report commun au client et au professionnel.
     * Possible seulement quand le rendez-vous est accepté et que les travaux n'ont pas commencé.
     */
    private RendezVousResponse reporter(Demande demande, String motif) {
        if (demande.getStatut() != StatutDemande.PLANIFIEE) {
            throw new BusinessException(
                    "Seul un rendez-vous planifié peut être reporté, avant le début des travaux (statut actuel : "
                            + demande.getStatut() + ")");
        }

        RendezVous rendezVous = chercherActuel(demande.getId());
        rendezVous.setStatut(StatutRendezVous.REPORTE);
        rendezVous.setMotif(limiter(motif));

        // Retour à DEVIS_ACCEPTE : le pro doit proposer une nouvelle date
        demande.setStatut(StatutDemande.DEVIS_ACCEPTE);

        return rendezVousMapper.toResponse(rendezVous);
    }

    // Règle 3 : le client ne répond qu'à une date qui attend sa réponse
    private void verifierEnAttenteDeReponse(RendezVous rendezVous) {
        if (rendezVous.getStatut() != StatutRendezVous.PROPOSE) {
            throw new BusinessException(
                    "Aucune date n'attend votre réponse (statut du rendez-vous : "
                            + rendezVous.getStatut() + ")");
        }
    }

    // Coupe le motif s'il dépasse la taille de la colonne
    private String limiter(String motif) {
        return motif.length() > TAILLE_MAX_MOTIF ? motif.substring(0, TAILLE_MAX_MOTIF) : motif;
    }

    private RendezVous chercherActuel(Long demandeId) {
        return rendezVousRepository.findFirstByDemandeIdOrderByIdDesc(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucun rendez-vous n'a encore été proposé pour la demande " + demandeId));
    }

    private Demande chercherDemandeDuPro(Long professionnelId, Long demandeId) {
        return demandeRepository.findByIdAndProfessionnelId(demandeId, professionnelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Demande " + demandeId + " introuvable pour ce professionnel"));
    }

    private Demande chercherDemandeDuClient(Long clientId, Long demandeId) {
        return demandeRepository.findByIdAndClientId(demandeId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Demande " + demandeId + " introuvable pour ce client"));
    }

    private void verifierDemandeExiste(Long demandeId) {
        if (!demandeRepository.existsById(demandeId)) {
            throw new ResourceNotFoundException("Demande introuvable : " + demandeId);
        }
    }
}