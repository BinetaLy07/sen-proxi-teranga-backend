package sn.senproxiteranga.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sn.senproxiteranga.backend.dto.ChangerMotDePasseRequest;
import sn.senproxiteranga.backend.dto.DemandeCodeRequest;
import sn.senproxiteranga.backend.dto.ReinitialiserMotDePasseRequest;
import sn.senproxiteranga.backend.security.SessionPrincipal;
import sn.senproxiteranga.backend.service.MotDePasseService;

import java.util.Map;

// Gestion du mot de passe.
// - PATCH /api/auth/mot-de-passe : compte connecté (règle "/api/auth/**" de SecurityConfig)
// - POST /oublie et /reinitialiser : PUBLICS (on a oublié son mot de passe, donc pas de badge)
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

    // Mot de passe oublié, étape 1 : toujours la MÊME réponse (on ne révèle pas si le numéro existe)
    @PostMapping("/oublie")
    public Map<String, String> demanderCode(@Valid @RequestBody DemandeCodeRequest request) {
        motDePasseService.demanderCode(request.telephone());
        return Map.of("message",
                "Si ce numéro correspond à un compte, un code vient d'être envoyé par SMS. "
                        + "Il est valable 10 minutes.");
    }

    // Mot de passe oublié, étape 2 : le code reçu + le nouveau mot de passe
    @PostMapping("/reinitialiser")
    public Map<String, String> reinitialiser(@Valid @RequestBody ReinitialiserMotDePasseRequest request) {
        motDePasseService.reinitialiser(request);
        return Map.of("message",
                "Mot de passe réinitialisé. Vous pouvez vous connecter avec votre nouveau mot de passe.");
    }
}