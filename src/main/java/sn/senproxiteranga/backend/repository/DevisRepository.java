package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.senproxiteranga.backend.domain.Devis;

import java.util.List;
import java.util.Optional;

@Repository
public interface DevisRepository extends JpaRepository<Devis, Long> {

    // Historique : toutes les versions du devis d'une demande (version 1, 2, 3)
    List<Devis> findByDemandeIdOrderByNumeroVersionAsc(Long demandeId);

    // Devis actuel : la version la plus récente du devis d'une demande
    Optional<Devis> findFirstByDemandeIdOrderByNumeroVersionDesc(Long demandeId);

    // Vérifie si un devis a déjà été envoyé pour cette demande
    boolean existsByDemandeId(Long demandeId);
}