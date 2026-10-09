package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.senproxiteranga.backend.domain.enums.TypeMessage;

import java.time.LocalDateTime;

// Un message envoyé par un utilisateur à un autre (client <-> professionnel).
// La date d'envoi est createdAt (dans BaseEntity).
//
// Un message peut être rattaché à une DEMANDE : il fait alors partie de la
// discussion de cette demande (ex : "Je passe demain à 10 h").
// Sans demande, c'est une "question générale" (ex : le client pose une question
// à un pro depuis son profil, avant de lui envoyer une demande).
//
// Un message est soit ÉCRIT (TEXTE), soit VOCAL (AUDIO).
// Pour un message vocal, le son est rangé dans le dossier "uploads" ;
// on garde ici son nom de fichier, son format et sa durée.
//
// L'expéditeur peut SUPPRIMER son message pendant 24 h (comme WhatsApp) :
// les 2 participants voient alors « Ce message a été supprimé ».
// Le texte reste dans la base pour l'administrateur (preuve en cas de litige) ;
// le son d'un vocal, lui, est vraiment effacé du dossier "uploads".
//
// Chacun peut aussi « Supprimer pour moi » n'importe quel message (le sien, celui de
// l'autre, ou une trace « supprimé ») : il disparaît seulement de SON écran.
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

    // TEXTE ou AUDIO.
    // columnDefinition : une simple colonne texte (pas un ENUM MySQL, qu'on ne pourrait
    // plus modifier facilement) ; les anciens messages reçoivent "TEXTE" automatiquement.
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "VARCHAR(10) NOT NULL DEFAULT 'TEXTE'")
    private TypeMessage type = TypeMessage.TEXTE;

    // Le texte du message. Pour un vocal : "Message vocal (0:12)",
    // ce qui s'affiche dans la liste des conversations et dans la notification.
    @Column(nullable = false, length = 1000)
    private String contenu;

    // ----- Seulement pour un message vocal -----

    // Nom du fichier dans "uploads" (ex : "a3f9...7b2d.webm"), choisi par le serveur
    @Column(name = "audio_nom", length = 100)
    private String audioNom;

    // Format du son (ex : "audio/webm"), pour que le navigateur sache le lire
    @Column(name = "audio_type", length = 50)
    private String audioType;

    // Durée en secondes (2 minutes maximum)
    @Column(name = "audio_duree")
    private Integer audioDuree;

    // Le destinataire a-t-il ouvert la conversation depuis ce message ?
    @Column(nullable = false)
    private boolean lu = false;

    // ----- Suppression -----

    // L'expéditeur a-t-il supprimé ce message ?
    // columnDefinition : les anciens messages reçoivent automatiquement "non supprimé" (0)
    @Column(nullable = false, columnDefinition = "BIT NOT NULL DEFAULT 0")
    private boolean supprime = false;

    // Quand il l'a supprimé (vide si le message n'est pas supprimé)
    @Column(name = "date_suppression")
    private LocalDateTime dateSuppression;

    // « Supprimer pour moi » : caché seulement chez l'expéditeur / chez le destinataire
    @Column(name = "masque_expediteur", nullable = false, columnDefinition = "BIT NOT NULL DEFAULT 0")
    private boolean masquePourExpediteur = false;

    @Column(name = "masque_destinataire", nullable = false, columnDefinition = "BIT NOT NULL DEFAULT 0")
    private boolean masquePourDestinataire = false;

    // Ce message est-il caché pour cet utilisateur ?
    public boolean estMasquePour(Long utilisateurId) {
        return (masquePourExpediteur && expediteur.getId().equals(utilisateurId))
                || (masquePourDestinataire && destinataire.getId().equals(utilisateurId));
    }
}
