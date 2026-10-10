package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import sn.senproxiteranga.backend.domain.enums.FamilleCategorie;

public record CategorieRequest(

        @NotBlank(message = "Le nom de la catégorie est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom,

        @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
        String description,

        // Facultatif : un emoji, ex. "🔧"
        @Size(max = 16, message = "L'icône est trop longue (un seul emoji)")
        String icone,

        // Facultatif : MAISON, BATIMENT, EVENEMENTS ou REPARATIONS
        FamilleCategorie famille
) {
}
