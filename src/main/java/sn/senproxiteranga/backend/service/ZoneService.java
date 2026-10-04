package sn.senproxiteranga.backend.service;

import sn.senproxiteranga.backend.domain.enums.TypeZone;
import sn.senproxiteranga.backend.dto.ZoneRequest;
import sn.senproxiteranga.backend.dto.ZoneResponse;

import java.util.List;

public interface ZoneService {

    ZoneResponse creer(ZoneRequest request);

    ZoneResponse modifier(Long id, ZoneRequest request);

    List<ZoneResponse> lister(TypeZone type);

    ZoneResponse trouverParId(Long id);

    // Les communes d'une région
    List<ZoneResponse> listerCommunes(Long regionId);

    // Les quartiers d'une commune
    List<ZoneResponse> listerQuartiers(Long communeId);

    void supprimer(Long id);
}