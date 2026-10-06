package sn.senproxiteranga.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sn.senproxiteranga.backend.dto.ModifierMonCompteRequest;
import sn.senproxiteranga.backend.dto.MonCompteResponse;
import sn.senproxiteranga.backend.security.SessionPrincipal;
import sn.senproxiteranga.backend.service.MonCompteService;

// « Mon compte » : compte connecté (règle "/api/auth/**" de SecurityConfig).
// L'identité vient du badge : chacun ne voit et ne modifie QUE son propre compte.
@RestController
@RequestMapping("/api/auth/moi")
@RequiredArgsConstructor
public class MonCompteController {

    private final MonCompteService monCompteService;

    @GetMapping
    public MonCompteResponse consulter(@AuthenticationPrincipal SessionPrincipal moi) {
        return monCompteService.consulter(moi.utilisateurId());
    }

    // Corps : {"prenom":"Awa","nom":"Diop","telephone":"771234567","adresse":"Liberté 6","zoneId":3}
    @PutMapping
    public MonCompteResponse modifier(@AuthenticationPrincipal SessionPrincipal moi,
                                      @Valid @RequestBody ModifierMonCompteRequest request) {
        return monCompteService.modifier(moi.utilisateurId(), request);
    }
}