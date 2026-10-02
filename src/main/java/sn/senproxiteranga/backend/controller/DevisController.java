package sn.senproxiteranga.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.senproxiteranga.backend.dto.DevisRequest;
import sn.senproxiteranga.backend.dto.DevisResponse;
import sn.senproxiteranga.backend.dto.MotifRequest;
import sn.senproxiteranga.backend.service.DevisService;

import java.util.List;

/**
 * Endpoints du module Devis.
 * Temporaire : les ids du professionnel et du client sont dans l'URL.
 * Ils seront remplacés par le token JWT quand on fera la sécurité.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DevisController {

    private final DevisService devisService;

    // ===================== Professionnel =====================

    // Envoyer le premier devis d'une demande acceptée
    @PostMapping("/professionnels/{proId}/demandes/{demandeId}/devis")
    @ResponseStatus(HttpStatus.CREATED)
    public DevisResponse envoyer(@PathVariable Long proId,
                                 @PathVariable Long demandeId,
                                 @Valid @RequestBody DevisRequest request) {
        return devisService.envoyer(proId, demandeId, request);
    }

    // Envoyer une nouvelle version après une demande de révision
    @PostMapping("/professionnels/{proId}/demandes/{demandeId}/devis/revision")
    @ResponseStatus(HttpStatus.CREATED)
    public DevisResponse reviser(@PathVariable Long proId,
                                 @PathVariable Long demandeId,
                                 @Valid @RequestBody DevisRequest request) {
        return devisService.reviser(proId, demandeId, request);
    }

    // ===================== Client =====================

    @PatchMapping("/clients/{clientId}/demandes/{demandeId}/devis/accepter")
    public DevisResponse accepter(@PathVariable Long clientId,
                                  @PathVariable Long demandeId) {
        return devisService.accepter(clientId, demandeId);
    }

    @PatchMapping("/clients/{clientId}/demandes/{demandeId}/devis/refuser")
    public DevisResponse refuser(@PathVariable Long clientId,
                                 @PathVariable Long demandeId,
                                 @Valid @RequestBody MotifRequest request) {
        return devisService.refuser(clientId, demandeId, request.motif());
    }

    @PatchMapping("/clients/{clientId}/demandes/{demandeId}/devis/demander-revision")
    public DevisResponse demanderRevision(@PathVariable Long clientId,
                                          @PathVariable Long demandeId,
                                          @Valid @RequestBody MotifRequest request) {
        return devisService.demanderRevision(clientId, demandeId, request.motif());
    }

    // ===================== Consultation =====================

    // Le devis actuel (dernière version)
    @GetMapping("/demandes/{demandeId}/devis")
    public DevisResponse trouverActuel(@PathVariable Long demandeId) {
        return devisService.trouverActuel(demandeId);
    }

    // Toutes les versions (historique)
    @GetMapping("/demandes/{demandeId}/devis/historique")
    public List<DevisResponse> historique(@PathVariable Long demandeId) {
        return devisService.historique(demandeId);
    }
}