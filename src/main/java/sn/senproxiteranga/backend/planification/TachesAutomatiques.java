package sn.senproxiteranga.backend.planification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sn.senproxiteranga.backend.domain.Demande;
import sn.senproxiteranga.backend.domain.Paiement;
import sn.senproxiteranga.backend.domain.enums.StatutDemande;
import sn.senproxiteranga.backend.domain.enums.StatutPaiement;
import sn.senproxiteranga.backend.domain.enums.TypeNotification;
import sn.senproxiteranga.backend.repository.DemandeRepository;
import sn.senproxiteranga.backend.repository.PaiementRepository;
import sn.senproxiteranga.backend.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

// Tâches lancées automatiquement par Spring, à intervalle régulier (grâce à PlanificationConfig).
// Toutes les minutes : assez souvent pour respecter le délai de 2 h des demandes urgentes,
// et sans coût réel (2 petites recherches dans la base à chaque passage).
@Slf4j
@Component
@RequiredArgsConstructor
public class TachesAutomatiques {

    private final DemandeRepository demandeRepository;
    private final PaiementRepository paiementRepository;
    private final NotificationService notificationService;

    // Délai de réponse : une demande CREEE sans réponse du professionnel
    // avant sa date d'expiration (2 h si urgente, 48 h sinon) passe en EXPIREE.
    // Le client est prévenu pour pouvoir choisir un autre professionnel.
    // Lancée toutes les minutes (60 000 ms), la 1re fois 30 s après le démarrage.
    @Scheduled(initialDelay = 30_000, fixedDelay = 60_000)
    @Transactional
    public void expirerDemandesSansReponse() {
        List<Demande> demandes = demandeRepository
                .findByStatutAndDateExpirationBefore(StatutDemande.CREEE, LocalDateTime.now());

        for (Demande demande : demandes) {
            String delai = demande.isUrgente() ? "2 h (demande urgente)" : "48 h";
            demande.setStatut(StatutDemande.EXPIREE);
            demande.setMotifAnnulation("Le professionnel n'a pas répondu dans le délai de " + delai);
            prevenirClientDemandeExpiree(demande, delai);
        }

        if (!demandes.isEmpty()) {
            log.info("Tâche automatique : {} demande(s) passée(s) en EXPIREE", demandes.size());
        }
    }

    // Règle des 48 h (paiement) : un paiement DECLARE que le professionnel n'a ni confirmé
    // ni contesté avant la date limite est confirmé automatiquement
    // ("qui ne dit mot consent"), et le dossier est clôturé. Client et pro sont prévenus.
    @Scheduled(initialDelay = 30_000, fixedDelay = 60_000)
    @Transactional
    public void confirmerPaiementsSansReponse() {
        LocalDateTime maintenant = LocalDateTime.now();
        List<Paiement> paiements = paiementRepository
                .findByStatutAndDateLimiteConfirmationBefore(StatutPaiement.DECLARE, maintenant);

        for (Paiement paiement : paiements) {
            paiement.setStatut(StatutPaiement.CONFIRME);
            paiement.setDateReponse(maintenant);
            paiement.getDemande().setStatut(StatutDemande.CLOTUREE);
            prevenirPaiementConfirmeAutomatiquement(paiement);
        }

        if (!paiements.isEmpty()) {
            log.info("Tâche automatique : {} paiement(s) confirmé(s) automatiquement "
                    + "(pas de réponse du professionnel)", paiements.size());
        }
    }

    // ---------- Notifications ----------

    private void prevenirClientDemandeExpiree(Demande demande, String delai) {
        String nomPro = demande.getProfessionnel().getPrenom() + " " + demande.getProfessionnel().getNom();
        notificationService.notifier(
                demande.getClient(),
                TypeNotification.DEMANDE_EXPIREE,
                "Demande expirée",
                nomPro + " n'a pas répondu dans le délai de " + delai
                        + " à votre demande : " + demande.getService().getTitre()
                        + ". Vous pouvez envoyer votre demande à un autre professionnel.",
                demande.getId());
    }

    private void prevenirPaiementConfirmeAutomatiquement(Paiement paiement) {
        Demande demande = paiement.getDemande();
        String montant = String.format("%.0f F CFA", paiement.getMontant());
        String message = "Le paiement de " + montant + " pour : " + demande.getService().getTitre()
                + " a été confirmé automatiquement (pas de réponse du professionnel sous 48 h). "
                + "Le dossier est clôturé.";

        notificationService.notifier(demande.getClient(),
                TypeNotification.PAIEMENT_CONFIRME, "Paiement confirmé", message, demande.getId());
        notificationService.notifier(demande.getProfessionnel(),
                TypeNotification.PAIEMENT_CONFIRME, "Paiement confirmé", message, demande.getId());
    }
}