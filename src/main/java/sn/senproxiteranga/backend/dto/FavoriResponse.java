package sn.senproxiteranga.backend.dto;

import java.time.LocalDateTime;

/**
 * Un professionnel dans la liste des favoris d'un client.
 */
public record FavoriResponse(
        Long id,
        LocalDateTime dateAjout,
        Long professionnelId,
        String professionnelNom,
        String metier,
        double noteMoyenne,
        String photo
) {
}