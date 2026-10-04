package sn.senproxiteranga.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sn.senproxiteranga.backend.dto.ProfessionnelResumeResponse;
import sn.senproxiteranga.backend.service.ProfessionnelService;

import java.util.List;

/**
 * Recherche et consultation des professionnels (accessible aux visiteurs).
 */
@RestController
@RequestMapping("/api/professionnels")
@RequiredArgsConstructor
public class ProfessionnelController {

    private final ProfessionnelService professionnelService;

    // Exemple : /api/professionnels/recherche?metier=plomb&zoneId=1&categorieId=1
    @GetMapping("/recherche")
    public List<ProfessionnelResumeResponse> rechercher(
            @RequestParam(required = false) String metier,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long categorieId) {
        return professionnelService.rechercher(metier, zoneId, categorieId);
    }
}