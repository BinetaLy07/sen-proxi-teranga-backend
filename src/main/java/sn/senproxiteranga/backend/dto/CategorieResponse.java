package sn.senproxiteranga.backend.dto;

import java.time.LocalDateTime;

public record CategorieResponse(
        Long id,
        String nom,
        String description,
        boolean active,
        LocalDateTime createdAt
) {
}