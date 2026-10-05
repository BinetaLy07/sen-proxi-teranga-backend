package sn.senproxiteranga.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.senproxiteranga.backend.dto.ConversationResponse;
import sn.senproxiteranga.backend.dto.MessageRequest;
import sn.senproxiteranga.backend.dto.MessageResponse;
import sn.senproxiteranga.backend.security.SessionPrincipal;
import sn.senproxiteranga.backend.service.MessageService;

import java.util.List;
import java.util.Map;

// Messagerie entre clients et professionnels.
// L'identité de l'utilisateur vient de son badge (token), jamais de l'URL.
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // Envoyer un message à quelqu'un : POST /api/messages/2  {"contenu": "Bonjour"}
    @PostMapping("/{destinataireId}")
    public ResponseEntity<MessageResponse> envoyer(@AuthenticationPrincipal SessionPrincipal moi,
                                                   @PathVariable Long destinataireId,
                                                   @Valid @RequestBody MessageRequest request) {
        MessageResponse envoye = messageService.envoyer(moi.utilisateurId(), destinataireId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(envoye);
    }

    // Ma liste de conversations
    @GetMapping("/conversations")
    public List<ConversationResponse> mesConversations(@AuthenticationPrincipal SessionPrincipal moi) {
        return messageService.mesConversations(moi.utilisateurId());
    }

    // Ouvrir la conversation avec quelqu'un (les messages reçus deviennent "lus")
    @GetMapping("/avec/{interlocuteurId}")
    public List<MessageResponse> conversation(@AuthenticationPrincipal SessionPrincipal moi,
                                              @PathVariable Long interlocuteurId) {
        return messageService.conversation(moi.utilisateurId(), interlocuteurId);
    }

    // Nombre de messages non lus : {"nonLus": 3}
    @GetMapping("/non-lus")
    public Map<String, Long> nombreNonLus(@AuthenticationPrincipal SessionPrincipal moi) {
        return Map.of("nonLus", messageService.nombreNonLus(moi.utilisateurId()));
    }
}