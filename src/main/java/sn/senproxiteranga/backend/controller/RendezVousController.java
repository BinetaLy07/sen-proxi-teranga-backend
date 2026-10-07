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
 * Les ids du client et du professionnel sont dans l'URL ;
 * la sécurité (EndpointAccess) vérifie qu'ils correspondent à la personne connectée.
 *
 * Le client propose la date ; l'autre accepte ou propose une autre date.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RendezVousController {

    private final RendezVousService rendezVousService;

    // ===================== Client =====================

    // Proposer une date : la première, ou une autre à la place de celle du professionnel
    @PostMapping("/clients/{clientId}/demandes/{demandeId}/rendez-vous")
    @ResponseStatus(HttpStatus.CREATED)
    public RendezVousResponse proposerParClient(@PathVariable Long clientId,
                                                @PathVariable Long demandeId,
                                                @Valid @RequestBody RendezVousRequest request) {
        return rendezVousService.proposerParClient(clientId, demandeId, request);
    }

    // Accepter la date proposée par le professionnel
    @PatchMapping("/clients/{clientId}/demandes/{demandeId}/rendez-vous/accepter")
    public RendezVousResponse accepterParClient(@PathVariable Long clientId,
                                                @PathVariable Long demandeId) {
        return rendezVousService.accepterParClient(clientId, demandeId);
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

    // ===================== Professionnel =====================

    // Proposer une autre date (la date du client ne convient pas)
    @PostMapping("/professionnels/{proId}/demandes/{demandeId}/rendez-vous")
    @ResponseStatus(HttpStatus.CREATED)
    public RendezVousResponse proposerParProfessionnel(@PathVariable Long proId,
                                                       @PathVariable Long demandeId,
                                                       @Valid @RequestBody RendezVousRequest request) {
        return rendezVousService.proposerParProfessionnel(proId, demandeId, request);
    }

    // Accepter la date proposée par le client
    @PatchMapping("/professionnels/{proId}/demandes/{demandeId}/rendez-vous/accepter")
    public RendezVousResponse accepterParProfessionnel(@PathVariable Long proId,
                                                       @PathVariable Long demandeId) {
        return rendezVousService.accepterParProfessionnel(proId, demandeId);
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
