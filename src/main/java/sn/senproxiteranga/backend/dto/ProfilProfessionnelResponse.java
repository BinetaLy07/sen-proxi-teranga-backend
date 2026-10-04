package sn.senproxiteranga.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Le profil public complet d'un professionnel, en une seule réponse.
 */
public record ProfilProfessionnelResponse(
        Long id,
        String prenom,
        String nom,
        String metier,
        String description,
        String competences,
        Integer experience,                 // null = non renseignée
        String photoUrl,                    // null = pas de photo de profil
        double noteMoyenne,
        long nombreAvis,
        LocalDateTime membreDepuis,         // date d'inscription

        List<String> zones,
        List<ServiceResponse> services,     // seulement les services actifs
        List<AvisResponse> avis,
        List<RealisationResponse> realisations
) {
}