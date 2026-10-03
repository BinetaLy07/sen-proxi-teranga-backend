package sn.senproxiteranga.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.senproxiteranga.backend.dto.AvisProfessionnelResponse;
import sn.senproxiteranga.backend.dto.AvisRequest;
import sn.senproxiteranga.backend.dto.AvisResponse;
import sn.senproxiteranga.backend.dto.ReponseAvisRequest;
import sn.senproxiteranga.backend.service.AvisService;

/**
 * Endpoints du module Avis.
 * Temporaire : les ids du client et du professionnel sont dans l'URL (remplacés plus tard par le JWT).
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AvisController {

    private final AvisService avisService;

    // ===================== Client =====================

    @PostMapping("/clients/{clientId}/demandes/{demandeId}/avis")
    @ResponseStatus(HttpStatus.CREATED)
    public AvisResponse donner(@PathVariable Long clientId,
                               @PathVariable Long demandeId,
                               @Valid @RequestBody AvisRequest request) {
        return avisService.donner(clientId, demandeId, request);
    }

    // ===================== Professionnel =====================

    @PatchMapping("/professionnels/{proId}/demandes/{demandeId}/avis/reponse")
    public AvisResponse repondre(@PathVariable Long proId,
                                 @PathVariable Long demandeId,
                                 @Valid @RequestBody ReponseAvisRequest request) {
        return avisService.repondre(proId, demandeId, request);
    }

    // ===================== Consultation (public) =====================

    @GetMapping("/demandes/{demandeId}/avis")
    public AvisResponse trouverParDemande(@PathVariable Long demandeId) {
        return avisService.trouverParDemande(demandeId);
    }

    @GetMapping("/professionnels/{proId}/avis")
    public AvisProfessionnelResponse listerParProfessionnel(@PathVariable Long proId) {
        return avisService.listerParProfessionnel(proId);
    }
}