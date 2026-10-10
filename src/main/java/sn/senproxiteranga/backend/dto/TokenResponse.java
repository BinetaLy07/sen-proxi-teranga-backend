package sn.senproxiteranga.backend.dto;

import java.time.Instant;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Instant accessExpiresAt,
        Instant refreshExpiresAt,
        Long utilisateurId,
        String role,
        String prenom,               // pour dire « Dalal ak diam, Moussa ! »
        String nom,
        boolean premiereConnexion    // true : 1re connexion après l'inscription (fenêtre de bienvenue)
) {}
