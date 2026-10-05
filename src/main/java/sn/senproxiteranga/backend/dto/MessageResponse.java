package sn.senproxiteranga.backend.dto;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long expediteurId,
        String expediteurNom,
        Long destinataireId,
        String destinataireNom,
        String contenu,
        LocalDateTime dateEnvoi,
        boolean lu
) {
}