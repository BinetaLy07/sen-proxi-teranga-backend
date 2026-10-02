package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.senproxiteranga.backend.domain.Utilisateur;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    // Pour la connexion : retrouver un compte par son email
    Optional<Utilisateur> findByEmailIgnoreCase(String email);

    // Pour l'inscription : cet email est-il déjà utilisé ?
    boolean existsByEmailIgnoreCase(String email);

    // Pour l'inscription : ce téléphone est-il déjà utilisé ?
    boolean existsByTelephone(String telephone);
}