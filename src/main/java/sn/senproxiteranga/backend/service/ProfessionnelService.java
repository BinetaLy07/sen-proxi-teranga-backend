package sn.senproxiteranga.backend.service;

import sn.senproxiteranga.backend.dto.ProfessionnelResumeResponse;

import java.util.List;

/**
 * Recherche et consultation des professionnels (visiteurs et clients).
 */
public interface ProfessionnelService {

    // Recherche avec filtres facultatifs : métier, zone, catégorie
    List<ProfessionnelResumeResponse> rechercher(String metier, Long zoneId, Long categorieId);
}