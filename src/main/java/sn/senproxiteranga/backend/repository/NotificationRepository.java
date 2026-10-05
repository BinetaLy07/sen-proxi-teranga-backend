package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.senproxiteranga.backend.domain.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Les notifications d'un utilisateur, les plus récentes d'abord
    List<Notification> findByDestinataireIdOrderByCreatedAtDescIdDesc(Long destinataireId);

    // Ses notifications pas encore lues
    List<Notification> findByDestinataireIdAndLueFalse(Long destinataireId);

    // Nombre de non lues (le chiffre rouge sur la cloche)
    long countByDestinataireIdAndLueFalse(Long destinataireId);

    // Une notification précise, à condition qu'elle appartienne à cet utilisateur
    Optional<Notification> findByIdAndDestinataireId(Long id, Long destinataireId);
}