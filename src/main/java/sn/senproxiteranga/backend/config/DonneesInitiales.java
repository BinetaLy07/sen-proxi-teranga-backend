package sn.senproxiteranga.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import sn.senproxiteranga.backend.domain.Zone;
import sn.senproxiteranga.backend.domain.enums.TypeZone;
import sn.senproxiteranga.backend.repository.ZoneRepository;

import java.util.List;

// Données de base créées automatiquement au démarrage de l'application
// (si elles n'existent pas encore dans la base)
@Slf4j
@Component
@RequiredArgsConstructor
public class DonneesInitiales implements CommandLineRunner {

    // Régions couvertes par la plateforme.
    // Lancement : région de Dakar uniquement.
    // Pour ouvrir une nouvelle région plus tard, il suffit de l'ajouter ici
    // (ex. "Dakar", "Thiès") puis de relancer l'application.
    private static final List<String> REGIONS_COUVERTES = List.of(
            "Dakar"
    );

    private final ZoneRepository zoneRepository;

    // Appelée automatiquement par Spring, une fois, juste après le démarrage
    @Override
    public void run(String... args) {
        creerRegions();
    }

    private void creerRegions() {
        int creees = 0;
        for (String nom : REGIONS_COUVERTES) {
            // On ne crée la région que si elle n'existe pas encore (pas de doublon)
            if (!zoneRepository.existsByNomIgnoreCaseAndType(nom, TypeZone.REGION)) {
                Zone region = new Zone();
                region.setNom(nom);
                region.setType(TypeZone.REGION);
                zoneRepository.save(region);
                creees++;
            }
        }
        log.info("Régions couvertes : {} créée(s), {} au total", creees, REGIONS_COUVERTES.size());
    }
}