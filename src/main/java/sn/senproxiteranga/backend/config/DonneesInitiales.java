package sn.senproxiteranga.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import sn.senproxiteranga.backend.domain.Categorie;
import sn.senproxiteranga.backend.domain.Zone;
import sn.senproxiteranga.backend.domain.enums.FamilleCategorie;
import sn.senproxiteranga.backend.domain.enums.TypeZone;
import sn.senproxiteranga.backend.repository.CategorieRepository;
import sn.senproxiteranga.backend.repository.ZoneRepository;

import java.util.List;

import static sn.senproxiteranga.backend.domain.enums.FamilleCategorie.BATIMENT;
import static sn.senproxiteranga.backend.domain.enums.FamilleCategorie.EVENEMENTS;
import static sn.senproxiteranga.backend.domain.enums.FamilleCategorie.MAISON;
import static sn.senproxiteranga.backend.domain.enums.FamilleCategorie.REPARATIONS;

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

    // Une catégorie de départ : son nom, son icône, sa famille et une courte description
    private record CategorieDeDepart(String nom, String icone, FamilleCategorie famille, String description) {
    }

    // Les services habituels d'un quartier, rangés en 4 familles.
    // Pour en ajouter une : une ligne ici, puis relancer l'application.
    private static final List<CategorieDeDepart> CATEGORIES_DE_DEPART = List.of(
            // ----- Maison & famille -----
            new CategorieDeDepart("Ménage & nettoyage", "🧹", MAISON, "Ménage de maison, bureau, grand nettoyage"),
            new CategorieDeDepart("Blanchisserie & repassage", "👕", MAISON, "Lessive, repassage, pressing"),
            new CategorieDeDepart("Cuisine & traiteur", "🍲", MAISON, "Cuisinière à domicile, plats pour cérémonies"),
            new CategorieDeDepart("Garde d'enfants (nounou)", "👶", MAISON, "Nounou à la journée ou au mois"),
            new CategorieDeDepart("Jardinage", "🌿", MAISON, "Entretien du jardin, arrosage, taille"),
            // ----- Bâtiment & travaux -----
            new CategorieDeDepart("Plomberie", "🔧", BATIMENT, "Fuites, robinets, sanitaires, chauffe-eau"),
            new CategorieDeDepart("Électricité", "💡", BATIMENT, "Installation, pannes, prises, tableau électrique"),
            new CategorieDeDepart("Maçonnerie", "🧱", BATIMENT, "Construction, carrelage, réparations de murs"),
            new CategorieDeDepart("Peinture", "🎨", BATIMENT, "Peinture intérieure et extérieure"),
            new CategorieDeDepart("Plafond & staff", "🏛️", BATIMENT, "Faux plafonds, staff, décoration de plafond"),
            new CategorieDeDepart("Menuiserie (bois, alu, métal)", "🪚", BATIMENT, "Portes, fenêtres, meubles, aluminium"),
            new CategorieDeDepart("Soudure", "🔥", BATIMENT, "Portails, grilles, réparations métalliques"),
            new CategorieDeDepart("Climatisation & froid", "❄️", BATIMENT, "Installation et entretien de climatiseurs, frigos"),
            // ----- Événements & fêtes -----
            new CategorieDeDepart("Location de matériel", "⛺", EVENEMENTS, "Bâches, chaises, tables, sono, marmites"),
            new CategorieDeDepart("Événementiel & décoration", "🎈", EVENEMENTS, "Décoration de baptêmes, mariages, anniversaires"),
            // ----- Réparations & services -----
            new CategorieDeDepart("Mécanique auto & moto", "🚗", REPARATIONS, "Entretien, pannes, vidange"),
            new CategorieDeDepart("Réparation téléphone & électroménager", "📱", REPARATIONS, "Téléphones, télé, frigo, machine à laver"),
            new CategorieDeDepart("Couture", "🧵", REPARATIONS, "Confection, retouches, tenues de cérémonie"),
            new CategorieDeDepart("Coiffure & beauté", "💇🏾‍♀️", REPARATIONS, "Coiffure à domicile, tresses, maquillage"),
            new CategorieDeDepart("Déménagement & transport", "🚚", REPARATIONS, "Déménagement, livraison, transport de matériel")
    );

    private final ZoneRepository zoneRepository;
    private final CategorieRepository categorieRepository;

    // Appelée automatiquement par Spring, une fois, juste après le démarrage
    @Override
    public void run(String... args) {
        creerRegions();
        creerCategories();
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

    // - Catégorie absente : on la crée (active).
    // - Catégorie déjà là (même nom, créée à la main par l'administrateur) : on ne la duplique pas ;
    //   on lui ajoute seulement l'icône et la famille si elle n'en a pas encore.
    //   On ne touche jamais à ce que l'administrateur a choisi (nom, description, active).
    private void creerCategories() {
        int creees = 0;
        int completees = 0;
        for (CategorieDeDepart depart : CATEGORIES_DE_DEPART) {
            Categorie existante = categorieRepository.findByNomIgnoreCase(depart.nom()).orElse(null);
            if (existante == null) {
                Categorie categorie = new Categorie();
                categorie.setNom(depart.nom());
                categorie.setDescription(depart.description());
                categorie.setIcone(depart.icone());
                categorie.setFamille(depart.famille());
                categorieRepository.save(categorie);
                creees++;
            } else if (existante.getIcone() == null || existante.getFamille() == null) {
                if (existante.getIcone() == null) {
                    existante.setIcone(depart.icone());
                }
                if (existante.getFamille() == null) {
                    existante.setFamille(depart.famille());
                }
                categorieRepository.save(existante);
                completees++;
            }
        }
        log.info("Catégories de départ : {} créée(s), {} complétée(s) (icône/famille), {} au total",
                creees, completees, CATEGORIES_DE_DEPART.size());
    }
}
