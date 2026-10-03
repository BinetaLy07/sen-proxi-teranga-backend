package sn.senproxiteranga.backend.dto;

import java.util.List;

/**
 * Le résumé des avis d'un professionnel, pour sa page de profil :
 * note moyenne, nombre d'avis et la liste des avis.
 */
public record AvisProfessionnelResponse(
        Long professionnelId,
        String professionnelNom,
        double noteMoyenne,         // ex : 4.7
        long nombreAvis,            // ex : 3
        List<AvisResponse> avis
) {
}