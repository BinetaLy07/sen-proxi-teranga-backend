package sn.senproxiteranga.backend.mapper;

import org.springframework.stereotype.Component;
import sn.senproxiteranga.backend.domain.Zone;
import sn.senproxiteranga.backend.dto.ZoneRequest;
import sn.senproxiteranga.backend.dto.ZoneResponse;

@Component
public class ZoneMapper {

    // Formulaire reçu -> nouvelle zone (la commune est ajoutée par le service)
    public Zone toEntity(ZoneRequest request) {
        Zone zone = new Zone();
        updateEntity(zone, request);
        return zone;
    }

    // Zone de la base -> réponse envoyée à Angular
    public ZoneResponse toResponse(Zone zone) {
        Zone commune = zone.getCommune();
        return new ZoneResponse(
                zone.getId(),
                zone.getNom(),
                zone.getType(),
                zone.getLatitude(),
                zone.getLongitude(),
                commune != null ? commune.getId() : null,
                commune != null ? commune.getNom() : null
        );
    }

    // Copie les champs simples du formulaire (la commune est gérée par le service)
    public void updateEntity(Zone zone, ZoneRequest request) {
        zone.setNom(request.nom().trim());
        zone.setType(request.type());
        zone.setLatitude(request.latitude());
        zone.setLongitude(request.longitude());
    }
}