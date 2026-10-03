package sn.senproxiteranga.backend.dto;

import java.time.LocalDateTime;

/**
 * Un avis tel qu'il est affiché.
 * Le nom du client est abrégé (ex : "Awa D.") car les avis sont publics.
 */
public record AvisResponse(
        Long id,
        int note,
        String commentaire,
        LocalDateTime dateAvis,
        String reponse,
        LocalDateTime dateReponse,

        // La demande concernée
        Long demandeId,
        String serviceTitre,

        // Le client (nom abrégé) et le professionnel
        String clientNom,
        Long professionnelId,
        String professionnelNom
) {
}