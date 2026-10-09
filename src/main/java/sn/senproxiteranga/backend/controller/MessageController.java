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
import sn.senproxiteranga.backend.dto.ConversationResponse;
import sn.senproxiteranga.backend.dto.MessageRequest;
import sn.senproxiteranga.backend.dto.MessageResponse;
import sn.senproxiteranga.backend.security.SessionPrincipal;
import sn.senproxiteranga.backend.service.MessageService;
import sn.senproxiteranga.backend.service.MessageService.FichierAudio;

import java.util.List;
import java.util.Map;

// Messagerie entre clients et professionnels.
// L'identité de l'utilisateur vient de son badge (token), jamais de l'URL.
//
// Deux sortes de discussions :
// - la discussion d'une DEMANDE : /api/demandes/{id}/messages
//   (la sécurité vérifie déjà que je suis le client ou le pro de cette demande) ;
// - les questions générales, sans demande : /api/messages/{destinataireId}
//
// Deux sortes de messages : écrits (JSON {"contenu": ...}) et vocaux
// (envoi de fichier "multipart/form-data" : champ "fichier" = le son, champ "duree" = secondes).
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // ===================== Discussion d'une demande =====================

    // Écrire dans la discussion d'une demande : POST /api/demandes/19/messages {"contenu": "Bonjour"}
    @PostMapping("/demandes/{demandeId}/messages")
    public ResponseEntity<MessageResponse> envoyerDansDemande(@AuthenticationPrincipal SessionPrincipal moi,
                                                              @PathVariable Long demandeId,
                                                              @Valid @RequestBody MessageRequest request) {
        MessageResponse envoye = messageService.envoyerDansDemande(moi.utilisateurId(), demandeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(envoye);
    }

    // Message vocal dans la discussion d'une demande : POST /api/demandes/19/messages/audio
    @PostMapping(value = "/demandes/{demandeId}/messages/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> envoyerAudioDansDemande(@AuthenticationPrincipal SessionPrincipal moi,
                                                                   @PathVariable Long demandeId,
                                                                   @RequestParam("fichier") MultipartFile fichier,
                                                                   @RequestParam("duree") int duree) {
        MessageResponse envoye = messageService.envoyerAudioDansDemande(
                moi.utilisateurId(), demandeId, fichier, duree);
        return ResponseEntity.status(HttpStatus.CREATED).body(envoye);
    }

    // Lire la discussion d'une demande (les messages reçus deviennent "lus")
    @GetMapping("/demandes/{demandeId}/messages")
    public List<MessageResponse> messagesDeLaDemande(@AuthenticationPrincipal SessionPrincipal moi,
                                                     @PathVariable Long demandeId) {
        return messageService.messagesDeLaDemande(moi.utilisateurId(), demandeId);
    }

    // ===================== Questions générales =====================

    // Écrire à quelqu'un sans parler d'une demande : POST /api/messages/2  {"contenu": "Bonjour"}
    @PostMapping("/messages/{destinataireId}")
    public ResponseEntity<MessageResponse> envoyer(@AuthenticationPrincipal SessionPrincipal moi,
                                                   @PathVariable Long destinataireId,
                                                   @Valid @RequestBody MessageRequest request) {
        MessageResponse envoye = messageService.envoyer(moi.utilisateurId(), destinataireId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(envoye);
    }

    // Message vocal sans demande : POST /api/messages/2/audio
    @PostMapping(value = "/messages/{destinataireId}/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> envoyerAudio(@AuthenticationPrincipal SessionPrincipal moi,
                                                        @PathVariable Long destinataireId,
                                                        @RequestParam("fichier") MultipartFile fichier,
                                                        @RequestParam("duree") int duree) {
        MessageResponse envoye = messageService.envoyerAudio(moi.utilisateurId(), destinataireId, fichier, duree);
        return ResponseEntity.status(HttpStatus.CREATED).body(envoye);
    }

    // Ouvrir les questions générales avec quelqu'un (les messages reçus deviennent "lus")
    @GetMapping("/messages/avec/{interlocuteurId}")
    public List<MessageResponse> conversation(@AuthenticationPrincipal SessionPrincipal moi,
                                              @PathVariable Long interlocuteurId) {
        return messageService.conversation(moi.utilisateurId(), interlocuteurId);
    }

    // ===================== Pour tout le monde =====================

    // Supprimer MON message (pendant 24 h) : DELETE /api/messages/57
    // Réponse : le message, devenu « Ce message a été supprimé »
    @DeleteMapping("/messages/{messageId}")
    public MessageResponse supprimer(@AuthenticationPrincipal SessionPrincipal moi,
                                     @PathVariable Long messageId) {
        return messageService.supprimer(moi.utilisateurId(), messageId);
    }

    // « Supprimer pour moi » : DELETE /api/messages/57/pour-moi
    // (le message disparaît seulement de MON écran ; l'autre et l'administrateur le voient toujours)
    @DeleteMapping("/messages/{messageId}/pour-moi")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void masquerPourMoi(@AuthenticationPrincipal SessionPrincipal moi,
                               @PathVariable Long messageId) {
        messageService.masquerPourMoi(moi.utilisateurId(), messageId);
    }

    // Écouter un message vocal : GET /api/messages/57/audio (renvoie le son lui-même)
    @GetMapping("/messages/{messageId}/audio")
    public ResponseEntity<Resource> ecouter(@AuthenticationPrincipal SessionPrincipal moi,
                                            @PathVariable Long messageId) {
        FichierAudio audio = messageService.chargerAudio(moi.utilisateurId(), messageId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(audio.contentType()))
                .body(audio.ressource());
    }

    // Ma liste de conversations (une par demande, plus les questions générales)
    @GetMapping("/messages/conversations")
    public List<ConversationResponse> mesConversations(@AuthenticationPrincipal SessionPrincipal moi) {
        return messageService.mesConversations(moi.utilisateurId());
    }

    // Nombre de messages non lus : {"nonLus": 3}
    @GetMapping("/messages/non-lus")
    public Map<String, Long> nombreNonLus(@AuthenticationPrincipal SessionPrincipal moi) {
        return Map.of("nonLus", messageService.nombreNonLus(moi.utilisateurId()));
    }
}
