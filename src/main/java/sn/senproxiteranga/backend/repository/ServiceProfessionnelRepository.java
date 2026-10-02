package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.senproxiteranga.backend.domain.ServiceProfessionnel;

import java.util.List;
import java.util.Optional;

public interface ServiceProfessionnelRepository extends JpaRepository<ServiceProfessionnel, Long> {

    // Tous les services d'un professionnel (pour son espace)
    List<ServiceProfessionnel> findByProfessionnelIdOrderByTitreAsc(Long professionnelId);

    // Seulement ses services actifs (pour les clients et visiteurs)
    List<ServiceProfessionnel> findByProfessionnelIdAndActifTrueOrderByTitreAsc(Long professionnelId);

    // Un service précis, à condition qu'il appartienne bien à ce professionnel
    Optional<ServiceProfessionnel> findByIdAndProfessionnelId(Long id, Long professionnelId);
}