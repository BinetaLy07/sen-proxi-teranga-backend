package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.senproxiteranga.backend.domain.enums.TypeNotification;

// Une notification affichée dans la "cloche" d'un utilisateur.
// La date est createdAt (dans BaseEntity).
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    // Celui qui reçoit la notification
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinataire_id", nullable = false)
    private Utilisateur destinataire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeNotification type;

    // Ex : "Nouvelle demande"
    @Column(nullable = false, length = 150)
    private String titre;

    // Ex : "Awa Diop vous a envoyé une demande : Fuite sous l'évier"
    @Column(nullable = false, length = 500)
    private String message;

    // La demande concernée, s'il y en a une (pour ouvrir la bonne page au clic)
    @Column(name = "demande_id")
    private Long demandeId;

    @Column(nullable = false)
    private boolean lue = false;
}