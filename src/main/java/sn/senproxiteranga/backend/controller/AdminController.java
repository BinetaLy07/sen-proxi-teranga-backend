package sn.senproxiteranga.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.senproxiteranga.backend.domain.enums.StatutVerification;
import sn.senproxiteranga.backend.dto.DemandeResponse;
import sn.senproxiteranga.backend.dto.MotifRequest;
import sn.senproxiteranga.backend.dto.ProfessionnelAdminResponse;
import sn.senproxiteranga.backend.dto.ResoudreLitigeRequest;
import sn.senproxiteranga.backend.dto.StatistiquesResponse;
import sn.senproxiteranga.backend.service.AdminService;

import java.util.List;

// Espace administrateur : vérification des professionnels, litiges et statistiques
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATEUR')")
public class AdminController {

    private final AdminService adminService;

    // ---------- Vérification des professionnels ----------

    // /api/admin/professionnels  ou  /api/admin/professionnels?statut=EN_ATTENTE
    @GetMapping("/professionnels")
    public List<ProfessionnelAdminResponse> listerProfessionnels(
            @RequestParam(required = false) StatutVerification statut) {
        return adminService.listerProfessionnels(statut);
    }

    @PatchMapping("/professionnels/{id}/valider")
    public ProfessionnelAdminResponse valider(@PathVariable Long id) {
        return adminService.validerProfessionnel(id);
    }

    // Corps : {"motif": "Votre photo de profil n'est pas nette"}
    @PatchMapping("/professionnels/{id}/correction")
    public ProfessionnelAdminResponse demanderCorrection(@PathVariable Long id,
                                                         @Valid @RequestBody MotifRequest request) {
        return adminService.demanderCorrection(id, request.motif());
    }

    // Corps : {"motif": "Faux profil"}
    @PatchMapping("/professionnels/{id}/refuser")
    public ProfessionnelAdminResponse refuser(@PathVariable Long id,
                                              @Valid @RequestBody MotifRequest request) {
        return adminService.refuserProfessionnel(id, request.motif());
    }

    // ---------- Litiges ----------

    @GetMapping("/litiges")
    public List<DemandeResponse> listerLitiges() {
        return adminService.listerLitiges();
    }

    // Corps : {"decision": "PAIEMENT_RECU", "explication": "Capture Wave vérifiée"}
    @PatchMapping("/litiges/{demandeId}/resoudre")
    public DemandeResponse resoudreLitige(@PathVariable Long demandeId,
                                          @Valid @RequestBody ResoudreLitigeRequest request) {
        return adminService.resoudreLitige(demandeId, request);
    }

    // ---------- Statistiques ----------

    @GetMapping("/statistiques")
    public StatistiquesResponse statistiques() {
        return adminService.statistiques();
    }
}