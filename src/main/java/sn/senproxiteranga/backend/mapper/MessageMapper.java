package sn.senproxiteranga.backend.mapper;

import org.springframework.stereotype.Component;
import sn.senproxiteranga.backend.domain.Demande;
import sn.senproxiteranga.backend.domain.Message;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.enums.TypeMessage;
import sn.senproxiteranga.backend.dto.ConversationResponse;
import sn.senproxiteranga.backend.dto.MessageResponse;

@Component
public class MessageMapper {

    // Ce que voient les 2 participants à la place d'un message supprimé
    public static final String TEXTE_SUPPRIME = "Ce message a été supprimé";

    // Nouveau message ÉCRIT (le service a déjà vérifié les règles).
    // demande : la demande dont on parle, ou null pour une question générale.
    public Message toEntity(Utilisateur expediteur, Utilisateur destinataire,
                            Demande demande, String contenu) {
        Message message = nouveau(expediteur, destinataire, demande);
        message.setType(TypeMessage.TEXTE);
        message.setContenu(contenu.trim());
        return message;
    }

    // Nouveau message VOCAL : le son est déjà rangé dans "uploads" sous "audioNom"
    public Message toAudioEntity(Utilisateur expediteur, Utilisateur destinataire, Demande demande,
                                 String audioNom, String audioType, int duree) {
        Message message = nouveau(expediteur, destinataire, demande);
        message.setType(TypeMessage.AUDIO);
        message.setContenu("Message vocal (" + duree / 60 + ":" + String.format("%02d", duree % 60) + ")");
        message.setAudioNom(audioNom);
        message.setAudioType(audioType);
        message.setAudioDuree(duree);
        return message;
    }

    // Message de la base -> réponse, pour un participant
    // (un message supprimé est remplacé par « Ce message a été supprimé »)
    public MessageResponse toResponse(Message message) {
        return toResponse(message, false);
    }

    // voirOriginal = true : pour l'administrateur, qui voit le texte même s'il a été supprimé
    public MessageResponse toResponse(Message message, boolean voirOriginal) {
        Utilisateur expediteur = message.getExpediteur();
        Utilisateur destinataire = message.getDestinataire();
        TypeMessage type = message.getType() != null ? message.getType() : TypeMessage.TEXTE;
        boolean masquer = message.isSupprime() && !voirOriginal;
        return new MessageResponse(
                message.getId(),
                expediteur.getId(),
                nomComplet(expediteur),
                destinataire.getId(),
                nomComplet(destinataire),
                masquer ? TEXTE_SUPPRIME : message.getContenu(),
                message.getCreatedAt(),
                message.isLu(),
                message.getDemande() != null ? message.getDemande().getId() : null,
                type.name(),
                masquer ? null : message.getAudioDuree(),
                message.isSupprime(),
                message.getDateSuppression()
        );
    }

    // Une ligne de la liste des conversations
    public ConversationResponse toConversation(Utilisateur interlocuteur, Message dernierMessage,
                                               Long moiId, long nombreNonLus) {
        Demande demande = dernierMessage.getDemande();
        return new ConversationResponse(
                interlocuteur.getId(),
                nomComplet(interlocuteur),
                interlocuteur.getRole().getNom().name(),
                demande != null ? demande.getId() : null,
                demande != null ? demande.getService().getTitre() : null,
                dernierMessage.isSupprime() ? "Message supprimé" : dernierMessage.getContenu(),
                dernierMessage.getCreatedAt(),
                dernierMessage.getExpediteur().getId().equals(moiId),
                nombreNonLus
        );
    }

    // Ce qui est commun aux messages écrits et vocaux
    private Message nouveau(Utilisateur expediteur, Utilisateur destinataire, Demande demande) {
        Message message = new Message();
        message.setExpediteur(expediteur);
        message.setDestinataire(destinataire);
        message.setDemande(demande);
        message.setLu(false);
        return message;
    }

    private String nomComplet(Utilisateur utilisateur) {
        return utilisateur.getPrenom() + " " + utilisateur.getNom();
    }
}
