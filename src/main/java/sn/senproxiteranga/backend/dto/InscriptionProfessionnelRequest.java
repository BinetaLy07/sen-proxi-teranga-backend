package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record InscriptionProfessionnelRequest(

        // ----- Informations communes (comme le client) -----
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

        // ----- Informations du métier -----
        @NotBlank(message = "Le métier est obligatoire")
        @Size(max = 100, message = "Le métier ne doit pas dépasser 100 caractères")
        String metier,

        String competences,

        @NotBlank(message = "La description de l'activité est obligatoire")
        @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
        String description,

        @Pattern(regexp = "^(\\+221)?7[05678]\\d{7}$",
                message = "Numéro WhatsApp invalide (ex : 771234567)")
        String whatsapp,

        // Ancienne façon : les zones choisies dans une liste (facultatif)
        List<Long> zoneIds,

        // Nouvelle façon : les zones écrites par le pro, séparées par des virgules,
        // ex : "Médina, Fass, Grand Yoff" (obligatoire)
        @NotBlank(message = "Écrivez au moins une zone d'intervention")
        @Size(max = 500, message = "Les zones ne doivent pas dépasser 500 caractères")
        String zones
) {
}