package sn.senproxiteranga.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sn.senproxiteranga.backend.dto.NotificationResponse;
import sn.senproxiteranga.backend.security.SessionPrincipal;
import sn.senproxiteranga.backend.service.NotificationService;

import java.util.List;
import java.util.Map;

// Les notifications de l'utilisateur CONNECTÉ (client, pro ou admin).
// L'identité vient toujours du badge (jeton), jamais de l'adresse.
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Ma liste de notifications, les plus récentes en premier
    @GetMapping
    public List<NotificationResponse> mesNotifications(
            @AuthenticationPrincipal SessionPrincipal moi) {
        return notificationService.mesNotifications(moi.utilisateurId());
    }

    // Le petit chiffre rouge sur la cloche
    @GetMapping("/non-lues")
    public Map<String, Long> nombreNonLues(
            @AuthenticationPrincipal SessionPrincipal moi) {
        return Map.of("nonLues", notificationService.nombreNonLues(moi.utilisateurId()));
    }

    // J'ai cliqué sur une notification : elle devient "lue"
    @PatchMapping("/{notificationId}/lue")
    public NotificationResponse marquerLue(
            @AuthenticationPrincipal SessionPrincipal moi,
            @PathVariable Long notificationId) {
        return notificationService.marquerLue(moi.utilisateurId(), notificationId);
    }

    // Bouton "Tout marquer comme lu" : renvoie combien ont été marquées
    @PatchMapping("/tout-lu")
    public Map<String, Integer> marquerToutesLues(
            @AuthenticationPrincipal SessionPrincipal moi) {
        return Map.of("marquees", notificationService.marquerToutesLues(moi.utilisateurId()));
    }
}