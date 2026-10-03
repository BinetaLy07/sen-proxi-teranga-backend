package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import sn.senproxiteranga.backend.domain.enums.NomRole;

import java.util.List;

public record CreationUtilisateurRequest(
        @NotBlank @Size(max = 100) String prenom,
        @NotBlank @Size(max = 100) String nom,
        @NotBlank @Pattern(regexp = "^(\\+221)?(7[05678]|33)\\d{7}$") String telephone,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 72) String motDePasse,
        @AssertTrue(message = "Vous devez accepter les CGU") boolean cguAcceptees,
        @NotNull NomRole role,
        @Size(max = 255) String adresse,
        @Positive Long zoneId,
        @Size(max = 100) String metier,
        String competences,
        String description,
        @Pattern(regexp = "^(\\+221)?7[05678]\\d{7}$") String whatsapp,
        List<@NotNull @Positive Long> zoneIds) {
    @AssertTrue(message = "Le métier est obligatoire pour un professionnel")
    public boolean isMetierValide() {
        return role != NomRole.PROFESSIONNEL || (metier != null && !metier.isBlank());
    }
}
