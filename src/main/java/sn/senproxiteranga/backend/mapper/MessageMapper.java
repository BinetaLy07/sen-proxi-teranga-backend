package sn.senproxiteranga.backend.mapper;

import org.springframework.stereotype.Component;
import sn.senproxiteranga.backend.domain.Message;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.dto.ConversationResponse;
import sn.senproxiteranga.backend.dto.MessageResponse;

@Component
public class MessageMapper {

    // Nouveau message (le service a déjà vérifié les règles)
    public Message toEntity(Utilisateur expediteur, Utilisateur destinataire, String contenu) {
        Message message = new Message();
        message.setExpediteur(expediteur);
        message.setDestinataire(destinataire);
        message.setContenu(contenu.trim());
        message.setLu(false);
        return message;
    }

    // Message de la base -> réponse
    public MessageResponse toResponse(Message message) {
        Utilisateur expediteur = message.getExpediteur();
        Utilisateur destinataire = message.getDestinataire();
        return new MessageResponse(
                message.getId(),
                expediteur.getId(),
                nomComplet(expediteur),
                destinataire.getId(),
                nomComplet(destinataire),
                message.getContenu(),
                message.getCreatedAt(),
                message.isLu()
        );
    }

    // Une ligne de la liste des conversations
    public ConversationResponse toConversation(Utilisateur interlocuteur, Message dernierMessage,
                                               Long moiId, long nombreNonLus) {
        return new ConversationResponse(
                interlocuteur.getId(),
                nomComplet(interlocuteur),
                interlocuteur.getRole().getNom().name(),
                dernierMessage.getContenu(),
                dernierMessage.getCreatedAt(),
                dernierMessage.getExpediteur().getId().equals(moiId),
                nombreNonLus
        );
    }

    private String nomComplet(Utilisateur utilisateur) {
        return utilisateur.getPrenom() + " " + utilisateur.getNom();
    }
}