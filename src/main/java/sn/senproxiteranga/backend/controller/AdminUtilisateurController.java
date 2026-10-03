package sn.senproxiteranga.backend.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sn.senproxiteranga.backend.dto.CreationUtilisateurRequest;
import sn.senproxiteranga.backend.dto.StatutCompteRequest;
import sn.senproxiteranga.backend.dto.UtilisateurResponse;
import sn.senproxiteranga.backend.service.AuthService;

@RestController
@RequestMapping("/api/admin/utilisateurs")
@RequiredArgsConstructor
public class AdminUtilisateurController {

    private final AuthService authService;

    @PostMapping
    public ResponseEntity<UtilisateurResponse> creer(
            @Valid @RequestBody CreationUtilisateurRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.creerUtilisateur(request));
    }

    @PatchMapping("/{id}/statut-compte")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public UtilisateurResponse changerStatutCompte(
            @PathVariable Long id, @Valid @RequestBody StatutCompteRequest request) {
        return authService.changerStatutCompte(id, request.statutCompte());
    }
}
