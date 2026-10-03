package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.senproxiteranga.backend.domain.RendezVous;
import sn.senproxiteranga.backend.domain.enums.StatutRendezVous;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {

    // Historique : tous les rendez-vous d'une demande, du plus ancien au plus récent
    List<RendezVous> findByDemandeIdOrderByIdAsc(Long demandeId);

    // Le rendez-vous le plus récent d'une demande
    Optional<RendezVous> findFirstByDemandeIdOrderByIdDesc(Long demandeId);

    // Existe-t-il déjà un rendez-vous "actif" (PROPOSE ou ACCEPTE) pour cette demande ?
    boolean existsByDemandeIdAndStatutIn(Long demandeId, Collection<StatutRendezVous> statuts);
}