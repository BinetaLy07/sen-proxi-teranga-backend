package sn.senproxiteranga.backend.dto;

import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.domain.enums.StatutVerification;

import java.time.LocalDateTime;
import java.util.List;

// Ce que voit l'administrateur pour vérifier un professionnel
public record ProfessionnelAdminResponse(
        Long id,
        String nomComplet,
        String telephone,
        String email,
        String metier,
        Integer experience,
        List<String> zones,
        String photoUrl,
        StatutVerification statutVerification,
        String motifVerification,
        StatutCompte statutCompte,
        LocalDateTime dateInscription
) {
}