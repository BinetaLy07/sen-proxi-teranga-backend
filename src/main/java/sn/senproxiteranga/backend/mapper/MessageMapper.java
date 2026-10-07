package sn.senproxiteranga.backend.mapper;

import org.springframework.stereotype.Component;
import sn.senproxiteranga.backend.domain.Demande;
import sn.senproxiteranga.backend.domain.Message;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.dto.ConversationResponse;
import sn.senproxiteranga.backend.dto.MessageResponse;

@Component
public class MessageMapper {

    // Nouveau message (le service a déjà vérifié les règles).
    // demande : la demande dont on parle, ou null pour une question générale.
    public Message toEntity(Utilisateur expediteur, Utilisateur destinataire,
                            Demande demande, String contenu) {
        Message message = new Message();
        message.setExpediteur(expediteur);
        message.setDestinataire(destinataire);
        message.setDemande(demande);
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
                message.isLu(),
                message.getDemande() != null ? message.getDemande().getId() : null
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
