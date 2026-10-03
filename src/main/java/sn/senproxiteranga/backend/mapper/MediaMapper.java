package sn.senproxiteranga.backend.mapper;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import sn.senproxiteranga.backend.domain.Demande;
import sn.senproxiteranga.backend.domain.MediaDemande;
import sn.senproxiteranga.backend.domain.enums.TypeMedia;
import sn.senproxiteranga.backend.dto.MediaResponse;

@Component
public class MediaMapper {

    /**
     * Crée la fiche d'un média à partir du fichier reçu.
     * Le type, le content type et le nom stocké sont décidés par le service (après vérification),
     * jamais pris tels quels de ce qu'envoie l'utilisateur.
     */
    public MediaDemande toEntity(Demande demande, MultipartFile fichier,
                                 TypeMedia type, String contentType, String nomStocke) {
        MediaDemande media = new MediaDemande();
        media.setDemande(demande);
        media.setType(type);
        media.setNomOriginal(nomOriginal(fichier));
        media.setContentType(contentType);
        media.setNomStocke(nomStocke);
        media.setTaille(fichier.getSize());
        return media;
    }

    /**
     * Fiche du média -> réponse JSON, avec l'adresse pour afficher le fichier.
     */
    public MediaResponse toResponse(MediaDemande media) {
        return new MediaResponse(
                media.getId(),
                media.getType(),
                media.getNomOriginal(),
                media.getContentType(),
                media.getTaille(),
                media.getCreatedAt(),
                media.getDemande().getId(),
                "/api/medias/" + media.getId() + "/fichier"
        );
    }

    /**
     * Nom d'origine du fichier, limité à 255 caractères (taille de la colonne).
     * Si le navigateur n'a pas envoyé de nom, on met "fichier".
     */
    private String nomOriginal(MultipartFile fichier) {
        String nom = fichier.getOriginalFilename();
        if (nom == null || nom.isBlank()) {
            return "fichier";
        }
        nom = nom.trim();
        return nom.length() > 255 ? nom.substring(0, 255) : nom;
    }
}