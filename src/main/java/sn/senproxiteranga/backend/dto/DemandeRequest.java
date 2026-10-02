package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DemandeRequest(

        @NotNull(message = "Le service demandé est obligatoire")
        Long serviceId,

        @NotBlank(message = "La description du besoin est obligatoire")
        @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
        String description,

        @NotBlank(message = "L'adresse est obligatoire")
        @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères")
        String adresse,

        @NotNull(message = "La date souhaitée est obligatoire")
        @FutureOrPresent(message = "La date souhaitée ne peut pas être dans le passé")
        LocalDate dateSouhaitee,

        // Facultatif : si absent, la demande n'est pas urgente
        Boolean urgente,

        // Facultatif : le quartier de l'intervention
        Long zoneId,

        // Facultatif : le client souhaite que le professionnel passe voir avant le devis
        // (si absent, pas de visite demandée)
        Boolean visiteDemandee
) {
}