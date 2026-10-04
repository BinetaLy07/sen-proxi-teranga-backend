package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.senproxiteranga.backend.domain.Zone;
import sn.senproxiteranga.backend.domain.enums.TypeZone;
import sn.senproxiteranga.backend.dto.ZoneRequest;
import sn.senproxiteranga.backend.dto.ZoneResponse;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.ZoneMapper;
import sn.senproxiteranga.backend.repository.ZoneRepository;
import sn.senproxiteranga.backend.service.ZoneService;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ZoneServiceImpl implements ZoneService {

    private final ZoneRepository zoneRepository;
    private final ZoneMapper zoneMapper;

    @Override
    public ZoneResponse creer(ZoneRequest request) {
        String nom = request.nom().trim();
        Zone region = trouverRegionParente(request);
        Zone commune = trouverCommuneParente(request);
        verifierDoublon(nom, request.type(), commune);

        Zone zone = zoneMapper.toEntity(request);
        zone.setRegion(region);
        zone.setCommune(commune);
        return zoneMapper.toResponse(zoneRepository.save(zone));
    }

    @Override
    public ZoneResponse modifier(Long id, ZoneRequest request) {
        Zone zone = chercher(id);
        String nouveauNom = request.nom().trim();

        // Règle 4 : une zone ne peut pas être rattachée à elle-même
        if (id.equals(request.regionId()) || id.equals(request.communeId())) {
            throw new BusinessException("Une zone ne peut pas être rattachée à elle-même");
        }

        // Règle 5 : une zone qui contient des sous-zones ne peut pas changer de type
        if (zone.getType() != request.type() && contientDesSousZones(id)) {
            throw new BusinessException("Cette zone contient des sous-zones : son type ne peut pas changer");
        }

        Zone region = trouverRegionParente(request);
        Zone commune = trouverCommuneParente(request);

        // Vérifier les doublons seulement si le nom, le type ou la commune change
        // (sinon la zone se trouverait elle-même comme "doublon")
        Long ancienneCommuneId = zone.getCommune() != null ? zone.getCommune().getId() : null;
        Long nouvelleCommuneId = commune != null ? commune.getId() : null;
        boolean identique = zone.getNom().equalsIgnoreCase(nouveauNom)
                && zone.getType() == request.type()
                && Objects.equals(ancienneCommuneId, nouvelleCommuneId);
        if (!identique) {
            verifierDoublon(nouveauNom, request.type(), commune);
        }

        zoneMapper.updateEntity(zone, request);
        zone.setRegion(region);
        zone.setCommune(commune);
        return zoneMapper.toResponse(zoneRepository.save(zone));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZoneResponse> lister(TypeZone type) {
        List<Zone> zones = (type == null)
                ? zoneRepository.findAllByOrderByNomAsc()
                : zoneRepository.findByTypeOrderByNomAsc(type);
        return zones.stream()
                .map(zoneMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ZoneResponse trouverParId(Long id) {
        return zoneMapper.toResponse(chercher(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZoneResponse> listerCommunes(Long regionId) {
        Zone region = chercher(regionId);
        if (region.getType() != TypeZone.REGION) {
            throw new BusinessException("Cette zone n'est pas une région");
        }
        return zoneRepository.findByRegionIdOrderByNomAsc(regionId).stream()
                .map(zoneMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZoneResponse> listerQuartiers(Long communeId) {
        Zone commune = chercher(communeId);
        if (commune.getType() != TypeZone.COMMUNE) {
            throw new BusinessException("Cette zone n'est pas une commune");
        }
        return zoneRepository.findByCommuneIdOrderByNomAsc(communeId).stream()
                .map(zoneMapper::toResponse)
                .toList();
    }

    @Override
    public void supprimer(Long id) {
        Zone zone = chercher(id);
        // Règle 6 : on ne supprime pas une zone qui a encore des sous-zones
        if (zoneRepository.existsByRegionId(id)) {
            throw new BusinessException("Impossible de supprimer cette région : elle contient encore des communes");
        }
        if (zoneRepository.existsByCommuneId(id)) {
            throw new BusinessException("Impossible de supprimer cette commune : elle contient encore des quartiers");
        }
        zoneRepository.delete(zone);
    }

    // ---------- Méthodes internes ----------

    // Règle 1 : une COMMUNE a une région ; une REGION et un QUARTIER n'en ont pas
    private Zone trouverRegionParente(ZoneRequest request) {
        if (request.type() != TypeZone.COMMUNE) {
            if (request.regionId() != null) {
                throw new BusinessException("Seule une commune peut être rattachée à une région");
            }
            return null;
        }
        if (request.regionId() == null) {
            throw new BusinessException("La région est obligatoire pour une commune");
        }
        Zone region = zoneRepository.findById(request.regionId())
                .orElseThrow(() -> new ResourceNotFoundException("Région introuvable : " + request.regionId()));
        if (region.getType() != TypeZone.REGION) {
            throw new BusinessException("La zone choisie n'est pas une région");
        }
        return region;
    }

    // Règle 2 : un QUARTIER a une commune ; une REGION et une COMMUNE n'en ont pas
    private Zone trouverCommuneParente(ZoneRequest request) {
        if (request.type() != TypeZone.QUARTIER) {
            if (request.communeId() != null) {
                throw new BusinessException("Seul un quartier peut être rattaché à une commune");
            }
            return null;
        }
        if (request.communeId() == null) {
            throw new BusinessException("La commune est obligatoire pour un quartier");
        }
        Zone commune = zoneRepository.findById(request.communeId())
                .orElseThrow(() -> new ResourceNotFoundException("Commune introuvable : " + request.communeId()));
        if (commune.getType() != TypeZone.COMMUNE) {
            throw new BusinessException("La zone choisie n'est pas une commune");
        }
        return commune;
    }

    // Règle 3 : pas de région ni de commune en double,
    // pas de quartier en double dans une même commune
    private void verifierDoublon(String nom, TypeZone type, Zone commune) {
        if (type == TypeZone.QUARTIER) {
            if (zoneRepository.existsByNomIgnoreCaseAndCommuneId(nom, commune.getId())) {
                throw new BusinessException("Ce quartier existe déjà dans cette commune");
            }
        } else if (zoneRepository.existsByNomIgnoreCaseAndType(nom, type)) {
            throw new BusinessException(type == TypeZone.REGION
                    ? "Cette région existe déjà"
                    : "Cette commune existe déjà");
        }
    }

    // Une zone a-t-elle des communes (si région) ou des quartiers (si commune) ?
    private boolean contientDesSousZones(Long id) {
        return zoneRepository.existsByRegionId(id) || zoneRepository.existsByCommuneId(id);
    }

    private Zone chercher(Long id) {
        return zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone introuvable : " + id));
    }
}