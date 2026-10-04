package sn.senproxiteranga.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.senproxiteranga.backend.dto.FavoriResponse;
import sn.senproxiteranga.backend.service.FavoriService;

import java.util.List;

/**
 * Les professionnels favoris d'un client.
 * Temporaire : l'id du client est dans l'URL (remplacé plus tard par le JWT).
 */
@RestController
@RequestMapping("/api/clients/{clientId}/favoris")
@RequiredArgsConstructor
public class FavoriController {

    private final FavoriService favoriService;

    @PostMapping("/{proId}")
    @ResponseStatus(HttpStatus.CREATED)
    public FavoriResponse ajouter(@PathVariable Long clientId, @PathVariable Long proId) {
        return favoriService.ajouter(clientId, proId);
    }

    @DeleteMapping("/{proId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirer(@PathVariable Long clientId, @PathVariable Long proId) {
        favoriService.retirer(clientId, proId);
    }

    @GetMapping
    public List<FavoriResponse> lister(@PathVariable Long clientId) {
        return favoriService.lister(clientId);
    }
}