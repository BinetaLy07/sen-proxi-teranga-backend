package sn.senproxiteranga.backend.dto;

import sn.senproxiteranga.backend.domain.enums.FamilleCategorie;

import java.time.LocalDateTime;

public record CategorieResponse(
        Long id,
        String nom,
        String description,
        boolean active,
        LocalDateTime createdAt,
        String icone,
        FamilleCategorie famille
) {
}
