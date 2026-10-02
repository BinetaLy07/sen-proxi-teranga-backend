package sn.senproxiteranga.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.senproxiteranga.backend.domain.enums.StatutDemande;
import sn.senproxiteranga.backend.dto.AccepterDemandeRequest;
import sn.senproxiteranga.backend.dto.DemandeRequest;
import sn.senproxiteranga.backend.dto.DemandeResponse;
import sn.senproxiteranga.backend.dto.MotifRequest;
import sn.senproxiteranga.backend.service.DemandeService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DemandeController {

    private final DemandeService demandeService;

    // =============== CÔTÉ CLIENT ===============

    @PostMapping("/clients/{clientId}/demandes")
    public ResponseEntity<DemandeResponse> creer(@PathVariable Long clientId,
                                                 @Valid @RequestBody DemandeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(demandeService.creer(clientId, request));
    }

    @PutMapping("/clients/{clientId}/demandes/{demandeId}")
    public DemandeResponse modifier(@PathVariable Long clientId, @PathVariable Long demandeId,
                                    @Valid @RequestBody DemandeRequest request) {
        return demandeService.modifier(clientId, demandeId, request);
    }

    @PatchMapping("/clients/{clientId}/demandes/{demandeId}/annuler")
    public DemandeResponse annulerParClient(@PathVariable Long clientId, @PathVariable Long demandeId,
                                            @Valid @RequestBody MotifRequest request) {
        return demandeService.annulerParClient(clientId, demandeId, request);
    }

    @GetMapping("/clients/{clientId}/demandes")
    public List<DemandeResponse> listerParClient(@PathVariable Long clientId) {
        return demandeService.listerParClient(clientId);
    }

    // =============== CÔTÉ PROFESSIONNEL ===============

    @GetMapping("/professionnels/{proId}/demandes")
    public List<DemandeResponse> listerParProfessionnel(@PathVariable Long proId,
                                                        @RequestParam(required = false) StatutDemande statut) {
        return demandeService.listerParProfessionnel(proId, statut);
    }

    // Le corps est facultatif : {"fraisVisite": 1000} seulement si le client a demandé une visite
    @PatchMapping("/professionnels/{proId}/demandes/{demandeId}/accepter")
    public DemandeResponse accepter(@PathVariable Long proId, @PathVariable Long demandeId,
                                    @Valid @RequestBody(required = false) AccepterDemandeRequest request) {
        return demandeService.accepter(proId, demandeId, request);
    }

    @PatchMapping("/professionnels/{proId}/demandes/{demandeId}/refuser")
    public DemandeResponse refuser(@PathVariable Long proId, @PathVariable Long demandeId,
                                   @Valid @RequestBody MotifRequest request) {
        return demandeService.refuser(proId, demandeId, request);
    }

    @PatchMapping("/professionnels/{proId}/demandes/{demandeId}/annuler")
    public DemandeResponse annulerParProfessionnel(@PathVariable Long proId, @PathVariable Long demandeId,
                                                   @Valid @RequestBody MotifRequest request) {
        return demandeService.annulerParProfessionnel(proId, demandeId, request);
    }

    // =============== COMMUN ===============

    @GetMapping("/demandes/{demandeId}")
    public DemandeResponse trouverParId(@PathVariable Long demandeId) {
        return demandeService.trouverParId(demandeId);
    }
}