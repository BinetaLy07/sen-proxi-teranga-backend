package sn.senproxiteranga.backend.dto;

import sn.senproxiteranga.backend.domain.enums.TypeTarif;

public record ServiceResponse(
        Long id,
        String titre,
        String description,
        TypeTarif typeTarif,
        Double montant,
        boolean actif,
        Long categorieId,
        String categorieNom,
        Long professionnelId,
        String professionnelNom
) {
}