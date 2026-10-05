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
import sn.senproxiteranga.backend.repository.DemandeRepository;
import sn.senproxiteranga.backend.repository.PaiementRepository;

import java.time.LocalDateTime;
import java.util.List;

// Tâches lancées automatiquement par Spring, à intervalle régulier (grâce à PlanificationConfig)
@Slf4j
@Component
@RequiredArgsConstructor
public class TachesAutomatiques {

    private final DemandeRepository demandeRepository;
    private final PaiementRepository paiementRepository;

    // Règle des 48 h (demande) : une demande CREEE sans réponse du professionnel
    // avant sa date d'expiration passe en EXPIREE.
    // Lancée toutes les minutes (60 000 ms), la 1re fois 30 s après le démarrage.
    @Scheduled(initialDelay = 30_000, fixedDelay = 60_000)
    @Transactional
    public void expirerDemandesSansReponse() {
        List<Demande> demandes = demandeRepository
                .findByStatutAndDateExpirationBefore(StatutDemande.CREEE, LocalDateTime.now());

        for (Demande demande : demandes) {
            demande.setStatut(StatutDemande.EXPIREE);
            demande.setMotifAnnulation("Le professionnel n'a pas répondu dans le délai de 48 h");
        }

        if (!demandes.isEmpty()) {
            log.info("Tâche automatique : {} demande(s) passée(s) en EXPIREE", demandes.size());
        }
    }

    // Règle des 48 h (paiement) : un paiement DECLARE que le professionnel n'a ni confirmé
    // ni contesté avant la date limite est confirmé automatiquement
    // ("qui ne dit mot consent"), et le dossier est clôturé.
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
        }

        if (!paiements.isEmpty()) {
            log.info("Tâche automatique : {} paiement(s) confirmé(s) automatiquement "
                    + "(pas de réponse du professionnel)", paiements.size());
        }
    }
}