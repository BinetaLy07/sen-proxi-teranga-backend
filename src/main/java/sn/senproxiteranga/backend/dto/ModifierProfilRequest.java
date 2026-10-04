package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Le professionnel modifie les informations de son profil.
 * Tous les champs sont facultatifs. La photo et les réalisations sont envoyées à part (fichiers).
 */
public record ModifierProfilRequest(

        @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
        String description,

        @Size(max = 2000, message = "Les compétences ne doivent pas dépasser 2000 caractères")
        String competences,

        // Nombre d'années d'expérience
        @Min(value = 0, message = "L'expérience ne peut pas être négative")
        @Max(value = 60, message = "L'expérience ne peut pas dépasser 60 ans")
        Integer experience,

        // Même format que lors de l'inscription
        @Pattern(regexp = "^(\\+221)?7[05678]\\d{7}$",
                message = "Numéro WhatsApp invalide (ex : 771234567)")
        String whatsapp,

        // Recevoir un SMS pour chaque nouvelle demande (les demandes urgentes envoient toujours un SMS)
        Boolean alerteSmsActive
) {
}