package sn.senproxiteranga.backend.dto;

import java.util.List;

/**
 * Carte résumée d'un professionnel, affichée dans les résultats de recherche.
 */
public record ProfessionnelResumeResponse(
        Long id,
        String nomComplet,
        String metier,
        String description,
        Integer experience,       // années d'expérience (null = non renseignée)
        double noteMoyenne,
        long nombreAvis,
        String photoUrl,          // adresse de la photo de profil (null = pas de photo)
        List<String> zones        // noms des quartiers / communes où il travaille
) {
}