package sn.senproxiteranga.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Se connecter avec son numéro de téléphone OU son email, et son mot de passe.
// Exemples : {"identifiant": "77 123 45 67", "motDePasse": "..."}
//            {"identifiant": "awa@gmail.com", "motDePasse": "..."}
// (@JsonAlias : l'ancien nom "email" est encore accepté)
public record ConnexionRequest(
        @NotBlank(message = "Saisissez votre téléphone ou votre email")
        @Size(max = 150)
        @JsonAlias("email")
        String identifiant,

        @NotBlank(message = "Saisissez votre mot de passe")
        @Size(max = 200)
        String motDePasse) {}
