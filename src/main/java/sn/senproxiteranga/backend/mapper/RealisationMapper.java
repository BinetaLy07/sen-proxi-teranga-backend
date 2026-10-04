package sn.senproxiteranga.backend.mapper;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import sn.senproxiteranga.backend.domain.Professionnel;
import sn.senproxiteranga.backend.domain.Realisation;
import sn.senproxiteranga.backend.dto.RealisationResponse;

@Component
public class RealisationMapper {

    /**
     * Nouvelle réalisation à partir de la photo envoyée.
     * Le content type et le nom stocké sont décidés par le service (après vérification).
     */
    public Realisation toEntity(Professionnel pro, MultipartFile fichier, String titre,
                                String description, String contentType, String nomStocke) {
        Realisation realisation = new Realisation();
        realisation.setProfessionnel(pro);
        realisation.setTitre(titre.trim());
        realisation.setDescription(
                (description == null || description.isBlank()) ? null : description.trim());
        realisation.setNomOriginal(nomOriginal(fichier));
        realisation.setContentType(contentType);
        realisation.setNomStocke(nomStocke);
        return realisation;
    }

    /**
     * Réalisation -> réponse JSON, avec l'adresse pour afficher la photo.
     */
    public RealisationResponse toResponse(Realisation realisation) {
        return new RealisationResponse(
                realisation.getId(),
                realisation.getTitre(),
                realisation.getDescription(),
                realisation.getCreatedAt(),
                "/api/realisations/" + realisation.getId() + "/fichier"
        );
    }

    // Nom d'origine du fichier, limité à 255 caractères
    private String nomOriginal(MultipartFile fichier) {
        String nom = fichier.getOriginalFilename();
        if (nom == null || nom.isBlank()) {
            return "photo";
        }
        nom = nom.trim();
        return nom.length() > 255 ? nom.substring(0, 255) : nom;
    }
}