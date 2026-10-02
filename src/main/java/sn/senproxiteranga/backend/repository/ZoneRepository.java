package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.senproxiteranga.backend.domain.Zone;
import sn.senproxiteranga.backend.domain.enums.TypeZone;

import java.util.List;

public interface ZoneRepository extends JpaRepository<Zone, Long> {

    // Une commune avec ce nom existe-t-elle déjà ?
    boolean existsByNomIgnoreCaseAndType(String nom, TypeZone type);

    // Un quartier avec ce nom existe-t-il déjà dans cette commune ?
    boolean existsByNomIgnoreCaseAndCommuneId(String nom, Long communeId);

    // Cette commune a-t-elle des quartiers ?
    boolean existsByCommuneId(Long communeId);

    List<Zone> findAllByOrderByNomAsc();

    List<Zone> findByTypeOrderByNomAsc(TypeZone type);

    // Les quartiers d'une commune, triés par nom
    List<Zone> findByCommuneIdOrderByNomAsc(Long communeId);
}