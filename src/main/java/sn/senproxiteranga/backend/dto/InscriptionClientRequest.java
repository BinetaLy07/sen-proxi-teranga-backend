package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record InscriptionClientRequest(

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
        String prenom,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom,

        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(regexp = "^(\\+221)?(7[05678]|33)\\d{7}$",
                message = "Numéro sénégalais invalide (ex : 771234567 ou +221771234567)")
        String telephone,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email n'est pas valide")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
        String motDePasse,

        @AssertTrue(message = "Vous devez accepter les conditions d'utilisation (CGU)")
        boolean cguAcceptees,

        // Où habite le client (obligatoire)
        @NotBlank(message = "L'adresse est obligatoire")
        @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères")
        String adresse,

        // Ancienne façon : le quartier choisi dans une liste (facultatif)
        Long zoneId,

        // Nouvelle façon : le quartier écrit par le client, ex : "Sacré-Cœur 3" (obligatoire)
        @NotBlank(message = "Le quartier est obligatoire")
        @Size(max = 100, message = "Le quartier ne doit pas dépasser 100 caractères")
        String quartier
) {
}