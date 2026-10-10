package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.senproxiteranga.backend.domain.Categorie;

import java.util.List;
import java.util.Optional;

public interface CategorieRepository extends JpaRepository<Categorie, Long> {

    boolean existsByNomIgnoreCase(String nom);

    // Pour les données de départ : retrouver une catégorie déjà créée à la main
    Optional<Categorie> findByNomIgnoreCase(String nom);

    List<Categorie> findByActiveTrueOrderByNomAsc();
}
