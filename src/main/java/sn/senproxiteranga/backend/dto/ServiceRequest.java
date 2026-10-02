package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import sn.senproxiteranga.backend.domain.enums.TypeTarif;

public record ServiceRequest(

        @NotBlank(message = "Le titre du service est obligatoire")
        @Size(max = 150, message = "Le titre ne doit pas dépasser 150 caractères")
        String titre,

        String description,

        @NotNull(message = "Le type de tarif est obligatoire : FIXE, A_PARTIR_DE ou SUR_DEVIS")
        TypeTarif typeTarif,

        // Obligatoire pour FIXE et A_PARTIR_DE, vide pour SUR_DEVIS (vérifié dans le service)
        @Positive(message = "Le montant doit être supérieur à 0")
        Double montant,

        @NotNull(message = "La catégorie est obligatoire")
        Long categorieId
) {
}