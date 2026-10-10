package sn.senproxiteranga.backend.mapper;

import org.springframework.stereotype.Component;
import sn.senproxiteranga.backend.domain.Categorie;
import sn.senproxiteranga.backend.dto.CategorieRequest;
import sn.senproxiteranga.backend.dto.CategorieResponse;

@Component
public class CategorieMapper {

    // Formulaire reçu -> nouvelle entité à enregistrer
    public Categorie toEntity(CategorieRequest request) {
        Categorie categorie = new Categorie();
        categorie.setNom(request.nom().trim());
        categorie.setDescription(request.description());
        categorie.setIcone(nettoyerIcone(request.icone()));
        categorie.setFamille(request.famille());
        return categorie;
    }

    // Entité de la base -> réponse envoyée à Angular
    public CategorieResponse toResponse(Categorie categorie) {
        return new CategorieResponse(
                categorie.getId(),
                categorie.getNom(),
                categorie.getDescription(),
                categorie.isActive(),
                categorie.getCreatedAt(),
                categorie.getIcone(),
                categorie.getFamille()
        );
    }

    // Formulaire de modification -> mise à jour d'une entité existante.
    // L'icône et la famille ne changent que si le formulaire les envoie
    // (un ancien formulaire sans ces champs ne les efface pas).
    public void updateEntity(Categorie categorie, CategorieRequest request) {
        categorie.setNom(request.nom().trim());
        categorie.setDescription(request.description());
        if (request.icone() != null) {
            categorie.setIcone(nettoyerIcone(request.icone()));
        }
        if (request.famille() != null) {
            categorie.setFamille(request.famille());
        }
    }

    // "  " -> null ; " 🔧 " -> "🔧"
    private String nettoyerIcone(String icone) {
        if (icone == null || icone.isBlank()) {
            return null;
        }
        return icone.trim();
    }
}
