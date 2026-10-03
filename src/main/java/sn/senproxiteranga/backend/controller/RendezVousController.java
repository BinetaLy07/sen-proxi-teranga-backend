package sn.senproxiteranga.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.senproxiteranga.backend.dto.MotifRequest;
import sn.senproxiteranga.backend.dto.RendezVousRequest;
import sn.senproxiteranga.backend.dto.RendezVousResponse;
import sn.senproxiteranga.backend.service.RendezVousService;

import java.util.List;

/**
 * Endpoints du module Rendez-vous.
 * Temporaire : les ids du professionnel et du client sont dans l'URL,
 * ils seront remplacés par le token JWT quand on fera la sécurité.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RendezVousController {

    private final RendezVousService rendezVousService;

    // ===================== Professionnel =====================

    @PostMapping("/professionnels/{proId}/demandes/{demandeId}/rendez-vous")
    @ResponseStatus(HttpStatus.CREATED)
    public RendezVousResponse proposer(@PathVariable Long proId,
                                       @PathVariable Long demandeId,
                                       @Valid @RequestBody RendezVousRequest request) {
        return rendezVousService.proposer(proId, demandeId, request);
    }

    @PatchMapping("/professionnels/{proId}/demandes/{demandeId}/rendez-vous/reporter")
    public RendezVousResponse reporterParProfessionnel(@PathVariable Long proId,
                                                       @PathVariable Long demandeId,
                                                       @Valid @RequestBody MotifRequest request) {
        return rendezVousService.reporterParProfessionnel(proId, demandeId, request.motif());
    }

    @PatchMapping("/professionnels/{proId}/demandes/{demandeId}/rendez-vous/commencer")
    public RendezVousResponse commencer(@PathVariable Long proId,
                                        @PathVariable Long demandeId) {
        return rendezVousService.commencer(proId, demandeId);
    }

    @PatchMapping("/professionnels/{proId}/demandes/{demandeId}/rendez-vous/terminer")
    public RendezVousResponse terminer(@PathVariable Long proId,
                                       @PathVariable Long demandeId) {
        return rendezVousService.terminer(proId, demandeId);
    }

    // ===================== Client =====================

    @PatchMapping("/clients/{clientId}/demandes/{demandeId}/rendez-vous/accepter")
    public RendezVousResponse accepter(@PathVariable Long clientId,
                                       @PathVariable Long demandeId) {
        return rendezVousService.accepter(clientId, demandeId);
    }

    @PatchMapping("/clients/{clientId}/demandes/{demandeId}/rendez-vous/refuser")
    public RendezVousResponse refuser(@PathVariable Long clientId,
                                      @PathVariable Long demandeId,
                                      @Valid @RequestBody MotifRequest request) {
        return rendezVousService.refuser(clientId, demandeId, request.motif());
    }

    @PatchMapping("/clients/{clientId}/demandes/{demandeId}/rendez-vous/reporter")
    public RendezVousResponse reporterParClient(@PathVariable Long clientId,
                                                @PathVariable Long demandeId,
                                                @Valid @RequestBody MotifRequest request) {
        return rendezVousService.reporterParClient(clientId, demandeId, request.motif());
    }

    @PatchMapping("/clients/{clientId}/demandes/{demandeId}/rendez-vous/confirmer-fin")
    public RendezVousResponse confirmerFinTravaux(@PathVariable Long clientId,
                                                  @PathVariable Long demandeId) {
        return rendezVousService.confirmerFinTravaux(clientId, demandeId);
    }

    // ===================== Consultation =====================

    @GetMapping("/demandes/{demandeId}/rendez-vous")
    public RendezVousResponse trouverActuel(@PathVariable Long demandeId) {
        return rendezVousService.trouverActuel(demandeId);
    }

    @GetMapping("/demandes/{demandeId}/rendez-vous/historique")
    public List<RendezVousResponse> historique(@PathVariable Long demandeId) {
        return rendezVousService.historique(demandeId);
    }
}
