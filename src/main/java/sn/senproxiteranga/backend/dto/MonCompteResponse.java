package sn.senproxiteranga.backend.dto;

import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.domain.enums.StatutVerification;

import java.time.LocalDateTime;

// Les informations de l'utilisateur connecté (page « Mon compte » et en-tête du site)
public record MonCompteResponse(
        Long id,
        String prenom,
        String nom,
        String email,
        String telephone,
        String role,
        StatutCompte statutCompte,

        // Seulement pour un professionnel (null pour un client ou l'admin)
        StatutVerification statutVerification,
        String motifVerification,

        // Facultatifs
        String adresse,
        Long zoneId,
        String zoneNom,

        LocalDateTime dateInscription
) {
}