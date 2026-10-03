package sn.senproxiteranga.backend.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import sn.senproxiteranga.backend.dto.ConnexionRequest;
import sn.senproxiteranga.backend.dto.InscriptionClientRequest;
import sn.senproxiteranga.backend.dto.InscriptionProfessionnelRequest;
import sn.senproxiteranga.backend.dto.RefreshRequest;
import sn.senproxiteranga.backend.dto.RegisterRequest;
import sn.senproxiteranga.backend.dto.TokenResponse;
import sn.senproxiteranga.backend.dto.UtilisateurResponse;
import sn.senproxiteranga.backend.security.SessionPrincipal;
import sn.senproxiteranga.backend.security.SessionService;
import sn.senproxiteranga.backend.service.AuthService;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SessionService sessionService;

    @PostMapping("/register")
    public ResponseEntity<UtilisateurResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/connexion")
    public TokenResponse connexion(@Valid @RequestBody ConnexionRequest request) {
        return sessionService.login(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return sessionService.refresh(request.refreshToken());
    }

    @PostMapping("/deconnexion")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal SessionPrincipal p) {
        sessionService.logout(p);
    }

    @PostMapping("/deconnexion-toutes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logoutAll(@AuthenticationPrincipal SessionPrincipal p) {
        sessionService.logoutAll(p);
    }

    @GetMapping("/sessions")
    public List<SessionService.SessionInfo> sessions(@AuthenticationPrincipal SessionPrincipal p) {
        return sessionService.list(p);
    }

    @DeleteMapping("/sessions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(@AuthenticationPrincipal SessionPrincipal p, @PathVariable Long id) {
        sessionService.revoke(p, id);
    }

    @PostMapping("/inscription/client")
    public ResponseEntity<UtilisateurResponse> inscrireClient(
            @Valid @RequestBody InscriptionClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.inscrireClient(request));
    }

    @PostMapping("/inscription/professionnel")
    public ResponseEntity<UtilisateurResponse> inscrireProfessionnel(
            @Valid @RequestBody InscriptionProfessionnelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.inscrireProfessionnel(request));
    }
}
