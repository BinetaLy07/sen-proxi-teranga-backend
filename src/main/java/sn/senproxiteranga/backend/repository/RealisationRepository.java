package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.senproxiteranga.backend.domain.Realisation;

import java.util.List;
import java.util.Optional;

@Repository
public interface RealisationRepository extends JpaRepository<Realisation, Long> {

    // Les réalisations d'un pro, de la plus récente à la plus ancienne
    List<Realisation> findByProfessionnelIdOrderByIdDesc(Long professionnelId);

    // Nombre de réalisations d'un pro (pour la règle des 10 maximum)
    long countByProfessionnelId(Long professionnelId);

    // Une réalisation précise, seulement si elle appartient bien à ce pro
    Optional<Realisation> findByIdAndProfessionnelId(Long id, Long professionnelId);
}