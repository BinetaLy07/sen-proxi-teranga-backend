package sn.senproxiteranga.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sn.senproxiteranga.backend.dto.MediaResponse;
import sn.senproxiteranga.backend.service.MediaDemandeService;
import sn.senproxiteranga.backend.service.MediaDemandeService.FichierMedia;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Photos et vidéos jointes aux demandes.
 * L'identité du client et l'accès aux fichiers sont vérifiés par la session Bearer.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MediaController {

    private final MediaDemandeService mediaService;

    // ===================== Client =====================

    // Envoi d'un ou plusieurs fichiers (format multipart/form-data, champ "fichiers")
    @PostMapping(value = "/clients/{clientId}/demandes/{demandeId}/medias",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<MediaResponse> ajouter(@PathVariable Long clientId,
                                       @PathVariable Long demandeId,
                                       @RequestParam("fichiers") List<MultipartFile> fichiers) {
        return mediaService.ajouter(clientId, demandeId, fichiers);
    }

    @DeleteMapping("/clients/{clientId}/demandes/{demandeId}/medias/{mediaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long clientId,
                          @PathVariable Long demandeId,
                          @PathVariable Long mediaId) {
        mediaService.supprimer(clientId, demandeId, mediaId);
    }

    // ===================== Consultation =====================

    // Liste des médias d'une demande (informations en JSON)
    @GetMapping("/demandes/{demandeId}/medias")
    public List<MediaResponse> lister(@PathVariable Long demandeId) {
        return mediaService.lister(demandeId);
    }

    // Le fichier lui-même : le navigateur l'affiche directement (image ou vidéo)
    @GetMapping("/medias/{mediaId}/fichier")
    public ResponseEntity<Resource> afficher(@PathVariable Long mediaId) {
        FichierMedia fichier = mediaService.charger(mediaId);

        // "inline" = afficher dans le navigateur (et non pas télécharger)
        ContentDisposition disposition = ContentDisposition.inline()
                .filename(fichier.nomOriginal(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fichier.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(fichier.ressource());
    }
}
