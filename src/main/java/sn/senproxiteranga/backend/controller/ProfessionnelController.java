package sn.senproxiteranga.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sn.senproxiteranga.backend.dto.ModifierProfilRequest;
import sn.senproxiteranga.backend.dto.ProfessionnelResumeResponse;
import sn.senproxiteranga.backend.dto.ProfilProfessionnelResponse;
import sn.senproxiteranga.backend.dto.RealisationResponse;
import sn.senproxiteranga.backend.security.SessionPrincipal;
import sn.senproxiteranga.backend.service.ProfessionnelService;
import sn.senproxiteranga.backend.service.ProfessionnelService.FichierImage;

import java.util.List;

/**
 * Recherche, profil public et espace profil des professionnels.
 * Temporaire : l'id du pro est dans l'URL (remplacé plus tard par le JWT).
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProfessionnelController {

    private final ProfessionnelService professionnelService;

    // ===================== Visiteurs et clients =====================

    // Exemple : /api/professionnels/recherche?metier=plomb&zoneId=1&categorieId=1
    @GetMapping("/professionnels/recherche")
    public List<ProfessionnelResumeResponse> rechercher(
            @RequestParam(required = false) String metier,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long categorieId) {
        return professionnelService.rechercher(metier, zoneId, categorieId);
    }

    @GetMapping("/professionnels/{proId}/profil")
    public ProfilProfessionnelResponse profilPublic(@PathVariable Long proId) {
        return professionnelService.profilPublic(proId);
    }

    // ===================== Espace du professionnel =====================

    @GetMapping("/professionnels/{proId}/mon-profil")
    public ProfilProfessionnelResponse monProfil(@PathVariable Long proId) {
        return professionnelService.monProfil(proId);
    }

    @PutMapping("/professionnels/{proId}/profil")
    public ProfilProfessionnelResponse modifierProfil(@PathVariable Long proId,
                                                      @Valid @RequestBody ModifierProfilRequest request) {
        return professionnelService.modifierProfil(proId, request);
    }

    // Envoi de la photo (multipart, champ "photo")
    @PostMapping(value = "/professionnels/{proId}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfilProfessionnelResponse changerPhoto(@PathVariable Long proId,
                                                    @RequestParam("photo") MultipartFile photo) {
        return professionnelService.changerPhoto(proId, photo);
    }

    // Envoi d'une réalisation (multipart : "photo", "titre", "description" facultative)
    @PostMapping(value = "/professionnels/{proId}/realisations", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RealisationResponse ajouterRealisation(@PathVariable Long proId,
                                                  @RequestParam("photo") MultipartFile photo,
                                                  @RequestParam(required = false) String titre,
                                                  @RequestParam(required = false) String description) {
        return professionnelService.ajouterRealisation(proId, photo, titre, description);
    }

    @DeleteMapping("/professionnels/{proId}/realisations/{realisationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerRealisation(@PathVariable Long proId, @PathVariable Long realisationId) {
        professionnelService.supprimerRealisation(proId, realisationId);
    }

    // ===================== Affichage des images =====================

    // Adresses publiques : "moi" vaut null pour un visiteur non connecté.
    // Si un badge est envoyé, on sait qui regarde (le pro lui-même ou l'admin).
    @GetMapping("/professionnels/{proId}/photo")
    public ResponseEntity<Resource> afficherPhoto(@PathVariable Long proId,
                                                  @AuthenticationPrincipal SessionPrincipal moi) {
        return image(professionnelService.chargerPhoto(proId, idDe(moi), estAdmin(moi)));
    }

    @GetMapping("/realisations/{realisationId}/fichier")
    public ResponseEntity<Resource> afficherRealisation(@PathVariable Long realisationId,
                                                        @AuthenticationPrincipal SessionPrincipal moi) {
        return image(professionnelService.chargerRealisation(realisationId, idDe(moi), estAdmin(moi)));
    }

    private Long idDe(SessionPrincipal moi) {
        return moi == null ? null : moi.utilisateurId();
    }

    private boolean estAdmin(SessionPrincipal moi) {
        return moi != null && "ADMINISTRATEUR".equals(moi.role());
    }

    // Réponse "image" : le navigateur l'affiche directement
    private ResponseEntity<Resource> image(FichierImage fichier) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fichier.contentType()))
                .body(fichier.ressource());
    }
}