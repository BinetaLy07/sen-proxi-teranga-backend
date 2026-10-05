package sn.senproxiteranga.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sn.senproxiteranga.backend.dto.ChangerMotDePasseRequest;
import sn.senproxiteranga.backend.security.SessionPrincipal;
import sn.senproxiteranga.backend.service.MotDePasseService;

import java.util.Map;

// Gestion du mot de passe.
// /api/auth/mot-de-passe est déjà protégé par la règle "/api/auth/**" (compte connecté) de SecurityConfig.
@RestController
@RequestMapping("/api/auth/mot-de-passe")
@RequiredArgsConstructor
public class MotDePasseController {

    private final MotDePasseService motDePasseService;

    // Changer mon mot de passe (je suis connecté et je connais l'ancien)
    @PatchMapping
    public Map<String, String> changer(@AuthenticationPrincipal SessionPrincipal moi,
                                       @Valid @RequestBody ChangerMotDePasseRequest request) {
        int fermees = motDePasseService.changer(moi.utilisateurId(), moi.sessionId(), request);
        return Map.of("message",
                "Mot de passe modifié. " + fermees + " autre(s) connexion(s) fermée(s).");
    }
}