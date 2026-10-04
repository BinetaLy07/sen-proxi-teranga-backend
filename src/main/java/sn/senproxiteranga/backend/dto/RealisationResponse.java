package sn.senproxiteranga.backend.dto;

import java.time.LocalDateTime;

/**
 * Une réalisation affichée dans la galerie du profil.
 * "url" est l'adresse pour afficher la photo.
 */
public record RealisationResponse(
        Long id,
        String titre,
        String description,
        LocalDateTime dateAjout,
        String url            // ex : "/api/realisations/1/fichier"
) {
}