package sn.senproxiteranga.backend.service.implementation;

import sn.senproxiteranga.backend.domain.enums.NomRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.senproxiteranga.backend.domain.Demande;
import sn.senproxiteranga.backend.domain.Devis;
import sn.senproxiteranga.backend.domain.Paiement;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.enums.ModePaiement;
import sn.senproxiteranga.backend.domain.enums.StatutDemande;
import sn.senproxiteranga.backend.domain.enums.StatutDevis;
import sn.senproxiteranga.backend.domain.enums.StatutPaiement;
import sn.senproxiteranga.backend.domain.enums.TypeNotification;
import sn.senproxiteranga.backend.dto.PaiementRequest;
import sn.senproxiteranga.backend.dto.PaiementResponse;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.PaiementMapper;
import sn.senproxiteranga.backend.repository.UtilisateurRepository;
import sn.senproxiteranga.backend.repository.DemandeRepository;
import sn.senproxiteranga.backend.repository.DevisRepository;
import sn.senproxiteranga.backend.repository.PaiementRepository;
import sn.senproxiteranga.backend.service.NotificationService;
import sn.senproxiteranga.backend.service.PaiementService;
import sn.senproxiteranga.backend.service.SmsService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaiementServiceImpl implements PaiementService {

    // Règle du cahier des charges : le pro a 48 h pour confirmer (ou contester) le paiement.
    // Sans réponse dans ce délai, le paiement est confirmé automatiquement (voir TachesAutomatiques)
    private static final int DELAI_CONFIRMATION_HEURES = 48;

    // Taille maximale de la colonne "motif_contestation"
    private static final int TAILLE_MAX_MOTIF = 500;

    private final PaiementRepository paiementRepository;
    private final DemandeRepository demandeRepository;
    private final DevisRepository devisRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PaiementMapper paiementMapper;
    private final NotificationService notificationService;
    private final SmsService smsService;

    // =====================================================================
    //                              CLIENT
    // =====================================================================

    @Override
    public PaiementResponse declarer(Long clientId, Long demandeId, PaiementRequest request) {
        Demande demande = chercherDemandeDuClient(clientId, demandeId);
        verifierPayable(demande);                                          // Règles 1 et 2

        Double montant = montantDuDevisAccepte(demandeId);                  // Règle 3
        Paiement paiement = paiementMapper.toEntity(
                demande, montant, request.modePaiement(), request.reference());

        // Règle 4 : le pro a 48 h pour confirmer ou contester
        paiement.setStatut(StatutPaiement.DECLARE);
        paiement.setDateLimiteConfirmation(LocalDateTime.now().plusHours(DELAI_CONFIRMATION_HEURES));

        Paiement enregistre = paiementRepository.save(paiement);
        prevenirProPaiementDeclare(enregistre);
        return paiementMapper.toResponse(enregistre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaiementResponse> listerParClient(Long clientId) {
        if (!utilisateurRepository.existsByIdAndRoleNom(clientId, NomRole.CLIENT)) {
            throw new ResourceNotFoundException("Client introuvable : " + clientId);
        }
        return paiementRepository.findByDemandeClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(paiementMapper::toResponse)
                .toList();
    }

    // =====================================================================
    //                           PROFESSIONNEL
    // =====================================================================

    @Override
    public PaiementResponse confirmer(Long professionnelId, Long demandeId) {
        Demande demande = chercherDemandeDuPro(professionnelId, demandeId);
        Paiement paiement = chercherPaiement(demandeId);
        verifierEnAttente(paiement);                                        // Règle 5

        paiement.setStatut(StatutPaiement.CONFIRME);
        paiement.setDateReponse(LocalDateTime.now());

        // Paiement confirmé => dossier terminé
        demande.setStatut(StatutDemande.CLOTUREE);

        return paiementMapper.toResponse(paiement);
    }

    @Override
    public PaiementResponse contester(Long professionnelId, Long demandeId, String motif) {
        Demande demande = chercherDemandeDuPro(professionnelId, demandeId);
        Paiement paiement = chercherPaiement(demandeId);
        verifierEnAttente(paiement);                                        // Règle 5

        paiement.setStatut(StatutPaiement.CONTESTE);
        paiement.setMotifContestation(limiter(motif.trim()));
        paiement.setDateReponse(LocalDateTime.now());

        // Désaccord sur le paiement => litige (traité par l'administrateur)
        demande.setStatut(StatutDemande.EN_LITIGE);

        return paiementMapper.toResponse(paiement);
    }

    @Override
    public PaiementResponse enregistrerEspeces(Long professionnelId, Long demandeId) {
        Demande demande = chercherDemandeDuPro(professionnelId, demandeId);
        verifierPayable(demande);                                          // Règles 1 et 2

        Double montant = montantDuDevisAccepte(demandeId);                  // Règle 3
        Paiement paiement = paiementMapper.toEntity(demande, montant, ModePaiement.ESPECES, null);

        // Règle 6 : c'est le pro qui a reçu l'argent, pas besoin de confirmation
        LocalDateTime maintenant = LocalDateTime.now();
        paiement.setStatut(StatutPaiement.CONFIRME);
        paiement.setDateLimiteConfirmation(maintenant);
        paiement.setDateReponse(maintenant);

        demande.setStatut(StatutDemande.CLOTUREE);

        return paiementMapper.toResponse(paiementRepository.save(paiement));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaiementResponse> listerParProfessionnel(Long professionnelId) {
        if (!utilisateurRepository.existsByIdAndRoleNom(professionnelId, NomRole.PROFESSIONNEL)) {
            throw new ResourceNotFoundException("Professionnel introuvable : " + professionnelId);
        }
        return paiementRepository.findByDemandeProfessionnelIdOrderByCreatedAtDesc(professionnelId).stream()
                .map(paiementMapper::toResponse)
                .toList();
    }

    // =====================================================================
    //                            CONSULTATION
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public PaiementResponse trouverParDemande(Long demandeId) {
        if (!demandeRepository.existsById(demandeId)) {
            throw new ResourceNotFoundException("Demande introuvable : " + demandeId);
        }
        return paiementMapper.toResponse(chercherPaiement(demandeId));
    }

    // =====================================================================
    //                           NOTIFICATIONS
    // =====================================================================

    // Le client dit avoir payé : le pro DOIT le savoir, car sans réponse de sa part
    // sous 48 h, le paiement sera confirmé automatiquement => notification + SMS
    private void prevenirProPaiementDeclare(Paiement paiement) {
        Demande demande = paiement.getDemande();
        Utilisateur client = demande.getClient();
        String nomClient = client.getPrenom() + " " + client.getNom();
        String montant = String.format("%.0f F CFA", paiement.getMontant());

        String message = nomClient + " déclare vous avoir payé " + montant
                + " par " + paiement.getModePaiement()
                + ". Confirmez ou contestez sous " + DELAI_CONFIRMATION_HEURES
                + " h, sinon le paiement sera confirmé automatiquement.";
        notificationService.notifier(demande.getProfessionnel(),
                TypeNotification.PAIEMENT_DECLARE, "Paiement à confirmer", message, demande.getId());

        String sms = "Sen Proxi Teranga : " + nomClient + " declare vous avoir paye " + montant
                + ". Confirmez sous " + DELAI_CONFIRMATION_HEURES + " h dans l'application.";
        smsService.envoyer(numeroPourSms(demande.getProfessionnel()), sms);
    }

    // Le SMS part sur le téléphone ; à défaut, sur le numéro WhatsApp
    private String numeroPourSms(Utilisateur utilisateur) {
        String telephone = utilisateur.getTelephone();
        if (telephone != null && !telephone.isBlank()) {
            return telephone;
        }
        return utilisateur.getWhatsapp();
    }

    // =====================================================================
    //                     MÉTHODES INTERNES (règles)
    // =====================================================================

    /**
     * Règle 1 : on paie seulement des travaux dont le client a confirmé la fin.
     * Règle 2 : une demande n'est payée qu'une seule fois.
     */
    private void verifierPayable(Demande demande) {
        if (demande.getStatut() != StatutDemande.CONFIRMEE) {
            throw new BusinessException(
                    "Le paiement n'est possible qu'après la confirmation de fin des travaux (statut actuel : "
                            + demande.getStatut() + ")");
        }
        if (paiementRepository.existsByDemandeId(demande.getId())) {
            throw new BusinessException("Un paiement a déjà été enregistré pour cette demande");
        }
    }

    /**
     * Règle 3 : le montant est celui du devis accepté, jamais saisi par quelqu'un.
     */
    private Double montantDuDevisAccepte(Long demandeId) {
        return devisRepository.findFirstByDemandeIdOrderByNumeroVersionDesc(demandeId)
                .filter(devis -> devis.getStatut() == StatutDevis.ACCEPTE)
                .map(Devis::getMontantTotal)
                .orElseThrow(() -> new BusinessException(
                        "Aucun devis accepté pour cette demande : impossible de calculer le montant"));
    }

    // Règle 5 : le pro ne répond qu'à un paiement en attente (DECLARE)
    private void verifierEnAttente(Paiement paiement) {
        if (paiement.getStatut() != StatutPaiement.DECLARE) {
            throw new BusinessException(
                    "Ce paiement a déjà été traité (statut : " + paiement.getStatut() + ")");
        }
    }

    private String limiter(String motif) {
        return motif.length() > TAILLE_MAX_MOTIF ? motif.substring(0, TAILLE_MAX_MOTIF) : motif;
    }

    private Paiement chercherPaiement(Long demandeId) {
        return paiementRepository.findByDemandeId(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucun paiement n'a encore été enregistré pour la demande " + demandeId));
    }

    private Demande chercherDemandeDuClient(Long clientId, Long demandeId) {
        return demandeRepository.findByIdAndClientId(demandeId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Demande " + demandeId + " introuvable pour ce client"));
    }

    private Demande chercherDemandeDuPro(Long professionnelId, Long demandeId) {
        return demandeRepository.findByIdAndProfessionnelId(demandeId, professionnelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Demande " + demandeId + " introuvable pour ce professionnel"));
    }
}