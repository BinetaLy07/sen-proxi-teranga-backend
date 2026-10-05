package sn.senproxiteranga.backend.planification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sn.senproxiteranga.backend.domain.Demande;
import sn.senproxiteranga.backend.domain.enums.StatutDemande;
import sn.senproxiteranga.backend.repository.DemandeRepository;

import java.time.LocalDateTime;
import java.util.List;

// Tâches lancées automatiquement par Spring, à intervalle régulier (grâce à PlanificationConfig)
@Slf4j
@Component
@RequiredArgsConstructor
public class TachesAutomatiques {

    private final DemandeRepository demandeRepository;

    // Règle des 48 h : une demande CREEE sans réponse du professionnel
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
}