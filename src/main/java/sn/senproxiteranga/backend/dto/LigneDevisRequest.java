package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import sn.senproxiteranga.backend.domain.enums.TypeLigneDevis;

/**
 * Une ligne envoyée par le professionnel.
 * Le montant n'est PAS envoyé : le serveur le calcule (quantité × prix unitaire).
 */
public record LigneDevisRequest(

        @NotNull(message = "Le type de ligne est obligatoire (MAIN_OEUVRE, MATERIEL ou DEPLACEMENT)")
        TypeLigneDevis type,

        @NotBlank(message = "Le libellé de la ligne est obligatoire")
        @Size(max = 150, message = "Le libellé ne doit pas dépasser 150 caractères")
        String libelle,

        // Facultatif : si absent, la quantité vaut 1
        @Min(value = 1, message = "La quantité doit être au moins 1")
        Integer quantite,

        @NotNull(message = "Le prix unitaire est obligatoire")
        @Positive(message = "Le prix unitaire doit être supérieur à 0")
        Double prixUnitaire
) {}