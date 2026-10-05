package sn.senproxiteranga.backend.service;

import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.enums.TypeNotification;
import sn.senproxiteranga.backend.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    // Utilisé par les AUTRES services (demande, paiement, admin...) pour prévenir quelqu'un
    void notifier(Utilisateur destinataire, TypeNotification type,
                  String titre, String message, Long demandeId);

    // Toutes mes notifications, les plus récentes en premier
    List<NotificationResponse> mesNotifications(Long utilisateurId);

    // Le petit chiffre rouge sur la cloche
    long nombreNonLues(Long utilisateurId);

    // Marquer UNE notification comme lue (seulement si elle m'appartient)
    NotificationResponse marquerLue(Long utilisateurId, Long notificationId);

    // Bouton "Tout marquer comme lu" : renvoie combien ont été marquées
    int marquerToutesLues(Long utilisateurId);
}