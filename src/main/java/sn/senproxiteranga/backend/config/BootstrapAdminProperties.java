package sn.senproxiteranga.backend.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.bootstrap-admin")
public record BootstrapAdminProperties(
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Pattern(regexp = "^(\\+221)?(7[05678]|33)\\d{7}$") String telephone,
        @NotBlank @Size(max = 100) String prenom,
        @NotBlank @Size(max = 100) String nom,
        @NotBlank @Size(min = 8, max = 72) String password) {}
