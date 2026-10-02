package sn.senproxiteranga.backend.dto;

import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.domain.enums.StatutVerification;

import java.time.LocalDateTime;

public record UtilisateurResponse(
        Long id,
        String prenom,
        String nom,
        String telephone,
        String email,
        String role,
        StatutCompte statutCompte,
        StatutVerification statutVerification,
        LocalDateTime dateInscription
) {
}