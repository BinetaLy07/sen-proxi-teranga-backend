package sn.senproxiteranga.backend.mapper;

import org.springframework.stereotype.Component;

import sn.senproxiteranga.backend.domain.Notification;
import sn.senproxiteranga.backend.dto.NotificationResponse;

@Component
public class NotificationMapper {

    // Notification de la base -> réponse envoyée à Angular
    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitre(),
                notification.getMessage(),
                notification.getDemandeId(),
                notification.isLue(),
                notification.getCreatedAt());
    }
}