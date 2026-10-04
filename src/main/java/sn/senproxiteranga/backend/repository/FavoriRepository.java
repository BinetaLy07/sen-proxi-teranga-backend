package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.senproxiteranga.backend.domain.Favori;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriRepository extends JpaRepository<Favori, Long> {

    // Les favoris d'un client, du plus récent au plus ancien
    List<Favori> findByClientIdOrderByIdDesc(Long clientId);

    // Ce professionnel est-il déjà dans les favoris de ce client ?
    boolean existsByClientIdAndProfessionnelId(Long clientId, Long professionnelId);

    // Le favori précis (pour le retirer)
    Optional<Favori> findByClientIdAndProfessionnelId(Long clientId, Long professionnelId);
}