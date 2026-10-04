package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.senproxiteranga.backend.domain.Zone;
import sn.senproxiteranga.backend.domain.enums.TypeZone;

import java.util.List;

public interface ZoneRepository extends JpaRepository<Zone, Long> {

    // Une zone de ce type avec ce nom existe-t-elle déjà ?
    // (sert pour les RÉGIONS et les COMMUNES)
    boolean existsByNomIgnoreCaseAndType(String nom, TypeZone type);

    // Un quartier avec ce nom existe-t-il déjà dans cette commune ?
    boolean existsByNomIgnoreCaseAndCommuneId(String nom, Long communeId);

    // Cette commune a-t-elle des quartiers ?
    boolean existsByCommuneId(Long communeId);

    // Cette région a-t-elle des communes ?
    boolean existsByRegionId(Long regionId);

    List<Zone> findAllByOrderByNomAsc();

    List<Zone> findByTypeOrderByNomAsc(TypeZone type);

    // Les communes d'une région, triées par nom
    List<Zone> findByRegionIdOrderByNomAsc(Long regionId);

    // Les quartiers d'une commune, triés par nom
    List<Zone> findByCommuneIdOrderByNomAsc(Long communeId);
}