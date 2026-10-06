package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.senproxiteranga.backend.domain.CodeReinitialisation;

import java.util.List;
import java.util.Optional;

public interface CodeReinitialisationRepository extends JpaRepository<CodeReinitialisation, Long> {

    // Le dernier code encore valable de cet utilisateur (le plus récent)
    Optional<CodeReinitialisation> findFirstByUtilisateurIdAndUtiliseFalseOrderByCreatedAtDescIdDesc(Long utilisateurId);

    // Tous ses codes pas encore utilisés (pour les annuler quand on en envoie un nouveau)
    List<CodeReinitialisation> findByUtilisateurIdAndUtiliseFalse(Long utilisateurId);
}