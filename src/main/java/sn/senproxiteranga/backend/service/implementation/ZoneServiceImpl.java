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
        Zone commune = trouverCommuneParente(request);
        verifierDoublon(nom, request.type(), commune);

        Zone zone = zoneMapper.toEntity(request);
        zone.setCommune(commune);
        return zoneMapper.toResponse(zoneRepository.save(zone));
    }

    @Override
    public ZoneResponse modifier(Long id, ZoneRequest request) {
        Zone zone = chercher(id);
        String nouveauNom = request.nom().trim();
        Zone commune = trouverCommuneParente(request);

        // Une commune qui a des quartiers ne peut pas devenir un quartier
        if (zone.getType() == TypeZone.COMMUNE && request.type() == TypeZone.QUARTIER
                && zoneRepository.existsByCommuneId(zone.getId())) {
            throw new BusinessException("Cette commune contient des quartiers : elle ne peut pas devenir un quartier");
        }

        // Vérifier les doublons seulement si le nom, le type ou la commune change
        Long ancienneCommuneId = zone.getCommune() != null ? zone.getCommune().getId() : null;
        Long nouvelleCommuneId = commune != null ? commune.getId() : null;
        boolean identique = zone.getNom().equalsIgnoreCase(nouveauNom)
                && zone.getType() == request.type()
                && Objects.equals(ancienneCommuneId, nouvelleCommuneId);
        if (!identique) {
            verifierDoublon(nouveauNom, request.type(), commune);
        }

        zoneMapper.updateEntity(zone, request);
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
        if (zoneRepository.existsByCommuneId(id)) {
            throw new BusinessException("Impossible de supprimer cette commune : elle contient encore des quartiers");
        }
        zoneRepository.delete(zone);
    }

    // ---------- Méthodes internes ----------

    // Règles 1 et 2 : un QUARTIER a une commune, une COMMUNE n'en a pas
    private Zone trouverCommuneParente(ZoneRequest request) {
        if (request.type() == TypeZone.COMMUNE) {
            if (request.communeId() != null) {
                throw new BusinessException("Une commune ne peut pas être rattachée à une autre commune");
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

    // Règle 3 : pas de commune en double, pas de quartier en double dans une même commune
    private void verifierDoublon(String nom, TypeZone type, Zone commune) {
        if (type == TypeZone.COMMUNE) {
            if (zoneRepository.existsByNomIgnoreCaseAndType(nom, TypeZone.COMMUNE)) {
                throw new BusinessException("Cette commune existe déjà");
            }
        } else if (zoneRepository.existsByNomIgnoreCaseAndCommuneId(nom, commune.getId())) {
            throw new BusinessException("Ce quartier existe déjà dans cette commune");
        }
    }

    private Zone chercher(Long id) {
        return zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone introuvable : " + id));
    }
}