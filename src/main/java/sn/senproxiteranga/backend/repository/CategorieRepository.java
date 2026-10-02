package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.senproxiteranga.backend.domain.Categorie;

import java.util.List;

public interface CategorieRepository extends JpaRepository<Categorie, Long> {

    boolean existsByNomIgnoreCase(String nom);

    List<Categorie> findByActiveTrueOrderByNomAsc();
}