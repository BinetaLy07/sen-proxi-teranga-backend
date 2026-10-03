package sn.senproxiteranga.backend.dto;

import sn.senproxiteranga.backend.domain.enums.TypeMedia;

import java.time.LocalDateTime;

/**
 * Une photo ou une vidéo renvoyée au frontend.
 * Le champ "url" est l'adresse à utiliser pour afficher le fichier.
 */
public record MediaResponse(
        Long id,
        TypeMedia type,
        String nomOriginal,
        String contentType,
        long taille,               // en octets
        LocalDateTime dateEnvoi,
        Long demandeId,
        String url                 // ex : "/api/medias/1/fichier"
) {
}