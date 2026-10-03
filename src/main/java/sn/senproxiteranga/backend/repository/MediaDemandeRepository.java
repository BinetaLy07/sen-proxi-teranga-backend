package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.senproxiteranga.backend.domain.MediaDemande;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaDemandeRepository extends JpaRepository<MediaDemande, Long> {

    // Tous les médias d'une demande, dans l'ordre où ils ont été envoyés
    List<MediaDemande> findByDemandeIdOrderByCreatedAtAsc(Long demandeId);

    // Nombre de médias déjà envoyés pour une demande (pour la règle des 5 maximum)
    long countByDemandeId(Long demandeId);

    // Un média précis, seulement s'il appartient bien à cette demande
    Optional<MediaDemande> findByIdAndDemandeId(Long id, Long demandeId);
}
