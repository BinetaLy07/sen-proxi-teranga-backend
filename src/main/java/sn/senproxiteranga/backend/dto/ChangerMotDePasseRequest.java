package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Ce que l'utilisateur connecté envoie pour changer son mot de passe
public record ChangerMotDePasseRequest(

        @NotBlank(message = "L'ancien mot de passe est obligatoire")
        String ancienMotDePasse,

        // 72 maximum : au-delà, BCrypt ignore la fin du mot de passe
        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = 8, max = 72, message = "Le nouveau mot de passe doit contenir entre 8 et 72 caractères")
        String nouveauMotDePasse
) {
}