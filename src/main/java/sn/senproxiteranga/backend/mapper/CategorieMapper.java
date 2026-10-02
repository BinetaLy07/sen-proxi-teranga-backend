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
        return categorie;
    }

    // Entité de la base -> réponse envoyée à Angular
    public CategorieResponse toResponse(Categorie categorie) {
        return new CategorieResponse(
                categorie.getId(),
                categorie.getNom(),
                categorie.getDescription(),
                categorie.isActive(),
                categorie.getCreatedAt()
        );
    }

    // Formulaire de modification -> mise à jour d'une entité existante
    public void updateEntity(Categorie categorie, CategorieRequest request) {
        categorie.setNom(request.nom().trim());
        categorie.setDescription(request.description());
    }
}