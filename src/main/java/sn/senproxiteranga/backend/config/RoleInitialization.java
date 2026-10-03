package sn.senproxiteranga.backend.config;

import jakarta.validation.Validator;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sn.senproxiteranga.backend.domain.Role;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.enums.NomRole;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.repository.RoleRepository;
import sn.senproxiteranga.backend.repository.UtilisateurRepository;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(BootstrapAdminProperties.class)
public class RoleInitialization implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapAdminProperties adminProperties;
    private final Validator validator;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (NomRole nom : NomRole.values()) {
            if (roleRepository.findByNom(nom).isEmpty()) {
                roleRepository.save(new Role(nom));
            }
        }
        creerAdministrateurSiAbsent();
    }

    private void creerAdministrateurSiAbsent() {
        String configuredEmail = adminProperties.email();
        if (configuredEmail == null || configuredEmail.isBlank()) {
            throw new IllegalStateException("Configurer security.bootstrap-admin.email");
        }
        String email = configuredEmail.trim().toLowerCase(Locale.ROOT);
        var existing = utilisateurRepository.findByEmailIgnoreCase(email);
        if (existing.isPresent()) {
            if (!existing.get().aRole(NomRole.ADMINISTRATEUR)) {
                throw new IllegalStateException(
                        "L'email du compte initial appartient à un compte non administrateur");
            }
            return;
        }

        if (!validator.validate(adminProperties).isEmpty()) {
            throw new IllegalStateException(
                    "Configuration du compte administrateur initial invalide : renseigner "
                            + "ADMIN_EMAIL, ADMIN_TELEPHONE, ADMIN_PRENOM, ADMIN_NOM et "
                            + "ADMIN_PASSWORD (8 à 72 caractères). Le mot de passe est requis "
                            + "uniquement si le compte est absent.");
        }
        if (adminProperties.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("ADMIN_PASSWORD ne doit pas dépasser 72 octets UTF-8");
        }

        String telephone = adminProperties.telephone().trim();
        if (telephone.startsWith("+221")) {
            telephone = telephone.substring(4);
        }
        if (utilisateurRepository.existsByTelephone(telephone)) {
            throw new IllegalStateException(
                    "Le téléphone du compte administrateur initial est déjà utilisé");
        }

        Role role =
                roleRepository
                        .findByNom(NomRole.ADMINISTRATEUR)
                        .orElseThrow(() -> new IllegalStateException("Rôle ADMINISTRATEUR absent"));
        Utilisateur admin = new Utilisateur();
        admin.setPrenom(adminProperties.prenom().trim());
        admin.setNom(adminProperties.nom().trim());
        admin.setEmail(email);
        admin.setTelephone(telephone);
        admin.setRole(role);
        admin.setStatutCompte(StatutCompte.ACTIF);
        admin.setMotDePasseHache(passwordEncoder.encode(adminProperties.password()));
        utilisateurRepository.save(admin);
    }
}
