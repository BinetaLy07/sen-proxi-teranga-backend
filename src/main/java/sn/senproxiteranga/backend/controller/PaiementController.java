package sn.senproxiteranga.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.senproxiteranga.backend.dto.MotifRequest;
import sn.senproxiteranga.backend.dto.PaiementRequest;
import sn.senproxiteranga.backend.dto.PaiementResponse;
import sn.senproxiteranga.backend.service.PaiementService;

import java.util.List;

/**
 * Endpoints du module Paiement.
 * Temporaire : les ids du professionnel et du client sont dans l'URL,
 * ils seront remplacés par le token JWT quand on fera la sécurité.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;

    // ===================== Client =====================

    @PostMapping("/clients/{clientId}/demandes/{demandeId}/paiement")
    @ResponseStatus(HttpStatus.CREATED)
    public PaiementResponse declarer(@PathVariable Long clientId,
                                     @PathVariable Long demandeId,
                                     @Valid @RequestBody PaiementRequest request) {
        return paiementService.declarer(clientId, demandeId, request);
    }

    @GetMapping("/clients/{clientId}/paiements")
    public List<PaiementResponse> listerParClient(@PathVariable Long clientId) {
        return paiementService.listerParClient(clientId);
    }

    // ===================== Professionnel =====================

    @PatchMapping("/professionnels/{proId}/demandes/{demandeId}/paiement/confirmer")
    public PaiementResponse confirmer(@PathVariable Long proId,
                                      @PathVariable Long demandeId) {
        return paiementService.confirmer(proId, demandeId);
    }

    @PatchMapping("/professionnels/{proId}/demandes/{demandeId}/paiement/contester")
    public PaiementResponse contester(@PathVariable Long proId,
                                      @PathVariable Long demandeId,
                                      @Valid @RequestBody MotifRequest request) {
        return paiementService.contester(proId, demandeId, request.motif());
    }

    @PostMapping("/professionnels/{proId}/demandes/{demandeId}/paiement/especes")
    @ResponseStatus(HttpStatus.CREATED)
    public PaiementResponse enregistrerEspeces(@PathVariable Long proId,
                                               @PathVariable Long demandeId) {
        return paiementService.enregistrerEspeces(proId, demandeId);
    }

    @GetMapping("/professionnels/{proId}/paiements")
    public List<PaiementResponse> listerParProfessionnel(@PathVariable Long proId) {
        return paiementService.listerParProfessionnel(proId);
    }

    // ===================== Consultation =====================

    @GetMapping("/demandes/{demandeId}/paiement")
    public PaiementResponse trouverParDemande(@PathVariable Long demandeId) {
        return paiementService.trouverParDemande(demandeId);
    }
}