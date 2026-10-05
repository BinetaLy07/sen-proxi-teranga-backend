package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Un message envoyé par un utilisateur à un autre (client <-> professionnel).
// La date d'envoi est createdAt (dans BaseEntity).
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "messages")
public class Message extends BaseEntity {

    // Celui qui écrit
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expediteur_id", nullable = false)
    private Utilisateur expediteur;

    // Celui qui reçoit
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinataire_id", nullable = false)
    private Utilisateur destinataire;

    @Column(nullable = false, length = 1000)
    private String contenu;

    // Le destinataire a-t-il ouvert la conversation depuis ce message ?
    @Column(nullable = false)
    private boolean lu = false;
}