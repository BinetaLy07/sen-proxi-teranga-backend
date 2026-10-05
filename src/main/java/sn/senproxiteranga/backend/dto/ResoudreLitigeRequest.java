package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sn.senproxiteranga.backend.domain.enums.DecisionLitige;

// Ce que l'administrateur envoie pour régler un litige
// Exemple : {"decision": "PAIEMENT_RECU", "explication": "Capture Wave vérifiée"}
public record ResoudreLitigeRequest(

        @NotNull(message = "La décision est obligatoire : PAIEMENT_RECU ou DOSSIER_ANNULE")
        DecisionLitige decision,

        @NotBlank(message = "L'explication est obligatoire")
        @Size(max = 500, message = "L'explication ne doit pas dépasser 500 caractères")
        String explication
) {
}