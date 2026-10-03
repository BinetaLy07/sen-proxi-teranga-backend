package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.*;

public record ConnexionRequest(
        @NotBlank @Email String email, @NotBlank @Size(max = 200) String motDePasse) {}
