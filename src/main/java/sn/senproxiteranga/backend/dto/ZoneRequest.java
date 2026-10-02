package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sn.senproxiteranga.backend.domain.enums.TypeZone;

public record ZoneRequest(

        @NotBlank(message = "Le nom de la zone est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom,

        @NotNull(message = "Le type est obligatoire : COMMUNE ou QUARTIER")
        TypeZone type,

        @DecimalMin(value = "-90.0", message = "La latitude doit être comprise entre -90 et 90")
        @DecimalMax(value = "90.0", message = "La latitude doit être comprise entre -90 et 90")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "La longitude doit être comprise entre -180 et 180")
        @DecimalMax(value = "180.0", message = "La longitude doit être comprise entre -180 et 180")
        Double longitude,

        // Obligatoire pour un QUARTIER, vide pour une COMMUNE (vérifié dans le service)
        Long communeId
) {
}