package sn.senproxiteranga.backend.dto;

import sn.senproxiteranga.backend.domain.enums.TypeZone;

public record ZoneResponse(
        Long id,
        String nom,
        TypeZone type,
        Double latitude,
        Double longitude,
        Long communeId,
        String communeNom
) {
}