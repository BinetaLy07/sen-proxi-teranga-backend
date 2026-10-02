package sn.senproxiteranga.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.senproxiteranga.backend.dto.InscriptionClientRequest;
import sn.senproxiteranga.backend.dto.InscriptionProfessionnelRequest;
import sn.senproxiteranga.backend.dto.UtilisateurResponse;
import sn.senproxiteranga.backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/inscription/client")
    public ResponseEntity<UtilisateurResponse> inscrireClient(
            @Valid @RequestBody InscriptionClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.inscrireClient(request));
    }

    @PostMapping("/inscription/professionnel")
    public ResponseEntity<UtilisateurResponse> inscrireProfessionnel(
            @Valid @RequestBody InscriptionProfessionnelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.inscrireProfessionnel(request));
    }
}