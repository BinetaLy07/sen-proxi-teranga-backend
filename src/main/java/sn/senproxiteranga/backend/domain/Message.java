package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Un message envoyé par un utilisateur à un autre (client <-> professionnel).
// La date d'envoi est createdAt (dans BaseEntity).
//
// Un message peut être rattaché à une DEMANDE : il fait alors partie de la
// discussion de cette demande (ex : "Je passe demain à 10 h").
// Sans demande, c'est une "question générale" (ex : le client pose une question
// à un pro depuis son profil, avant de lui envoyer une demande).
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

    // La demande dont on parle (vide pour une question générale)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_id")
    private Demande demande;

    @Column(nullable = false, length = 1000)
    private String contenu;

    // Le destinataire a-t-il ouvert la conversation depuis ce message ?
    @Column(nullable = false)
    private boolean lu = false;
}
