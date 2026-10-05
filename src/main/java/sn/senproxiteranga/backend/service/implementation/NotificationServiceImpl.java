package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.senproxiteranga.backend.domain.Notification;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.enums.TypeNotification;
import sn.senproxiteranga.backend.dto.NotificationResponse;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.NotificationMapper;
import sn.senproxiteranga.backend.repository.NotificationRepository;
import sn.senproxiteranga.backend.service.NotificationService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    // =====================================================================
    //          CRÉER UNE NOTIFICATION (appelé par les autres services)
    // =====================================================================

    @Override
    public void notifier(Utilisateur destinataire, TypeNotification type,
                         String titre, String message, Long demandeId) {
        // Sécurité : sans destinataire, on ne fait rien (pas de plantage)
        if (destinataire == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setDestinataire(destinataire);
        notification.setType(type);
        notification.setTitre(titre);
        notification.setMessage(message);
        notification.setDemandeId(demandeId);
        notification.setLue(false);
        notificationRepository.save(notification);
    }

    // =====================================================================
    //                       LIRE MES NOTIFICATIONS
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> mesNotifications(Long utilisateurId) {
        return notificationRepository
                .findByDestinataireIdOrderByCreatedAtDescIdDesc(utilisateurId)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long nombreNonLues(Long utilisateurId) {
        return notificationRepository.countByDestinataireIdAndLueFalse(utilisateurId);
    }

    // =====================================================================
    //                       MARQUER COMME LUE(S)
    // =====================================================================

    @Override
    public NotificationResponse marquerLue(Long utilisateurId, Long notificationId) {
        // On cherche avec l'id ET le propriétaire : la notification d'un autre
        // utilisateur est "introuvable" pour moi (on ne révèle pas qu'elle existe)
        Notification notification = notificationRepository
                .findByIdAndDestinataireId(notificationId, utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification introuvable : " + notificationId));

        // Pas besoin de save() : @Transactional enregistre le changement tout seul
        notification.setLue(true);
        return notificationMapper.toResponse(notification);
    }

    @Override
    public int marquerToutesLues(Long utilisateurId) {
        List<Notification> nonLues =
                notificationRepository.findByDestinataireIdAndLueFalse(utilisateurId);
        nonLues.forEach(n -> n.setLue(true));
        return nonLues.size();
    }
}