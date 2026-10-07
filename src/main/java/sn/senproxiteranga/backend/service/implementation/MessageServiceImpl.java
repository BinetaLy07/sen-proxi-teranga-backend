package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.senproxiteranga.backend.domain.Demande;
import sn.senproxiteranga.backend.domain.Message;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.enums.NomRole;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.domain.enums.StatutVerification;
import sn.senproxiteranga.backend.domain.enums.TypeNotification;
import sn.senproxiteranga.backend.dto.ConversationResponse;
import sn.senproxiteranga.backend.dto.MessageRequest;
import sn.senproxiteranga.backend.dto.MessageResponse;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.MessageMapper;
import sn.senproxiteranga.backend.repository.DemandeRepository;
import sn.senproxiteranga.backend.repository.MessageRepository;
import sn.senproxiteranga.backend.repository.UtilisateurRepository;
import sn.senproxiteranga.backend.service.MessageService;
import sn.senproxiteranga.backend.service.NotificationService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageServiceImpl implements MessageService {

    // Longueur de l'extrait du message affiché dans la notification
    private static final int TAILLE_EXTRAIT = 100;

    private final MessageRepository messageRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DemandeRepository demandeRepository;
    private final MessageMapper messageMapper;
    private final NotificationService notificationService;

    // =====================================================================
    //               QUESTIONS GÉNÉRALES (messages sans demande)
    // =====================================================================

    @Override
    public MessageResponse envoyer(Long expediteurId, Long destinataireId, MessageRequest request) {
        // Règle 2 : on ne s'écrit pas à soi-même
        if (expediteurId.equals(destinataireId)) {
            throw new BusinessException("Vous ne pouvez pas vous écrire à vous-même");
        }
        Utilisateur expediteur = chercherUtilisateur(expediteurId);
        Utilisateur destinataire = chercherUtilisateur(destinataireId);

        // Règle 3 : seulement entre un client et un professionnel
        if (expediteur.aRole(NomRole.CLIENT) && destinataire.aRole(NomRole.PROFESSIONNEL)) {
            verifierProDisponible(destinataire);                              // Règle 4
        } else if (expediteur.aRole(NomRole.PROFESSIONNEL) && destinataire.aRole(NomRole.CLIENT)) {
            verifierClientAContacte(destinataire, expediteur);                 // Règle 5
        } else {
            throw new BusinessException(
                    "Les messages s'échangent uniquement entre un client et un professionnel");
        }

        // Règle 1 (texte non vide, 1000 caractères max) : vérifiée par @Valid sur MessageRequest
        return enregistrer(expediteur, destinataire, null, request.contenu());
    }

    @Override
    public List<MessageResponse> conversation(Long utilisateurId, Long interlocuteurId) {
        chercherUtilisateur(interlocuteurId);

        // Ouvrir la conversation = lire les messages reçus : ils deviennent "lus"
        messageRepository
                .findByExpediteurIdAndDestinataireIdAndDemandeIsNullAndLuFalse(interlocuteurId, utilisateurId)
                .forEach(message -> message.setLu(true));

        return messageRepository.conversationGenerale(utilisateurId, interlocuteurId).stream()
                .map(messageMapper::toResponse)
                .toList();
    }

    // =====================================================================
    //                      DISCUSSION D'UNE DEMANDE
    // =====================================================================

    @Override
    public MessageResponse envoyerDansDemande(Long expediteurId, Long demandeId, MessageRequest request) {
        Demande demande = chercherDemande(demandeId);
        Utilisateur client = demande.getClient();
        Utilisateur pro = demande.getProfessionnel();

        // Règle 7 : seuls le client et le professionnel de la demande y écrivent.
        // Le message va toujours à "l'autre".
        if (client.getId().equals(expediteurId)) {
            return enregistrer(client, pro, demande, request.contenu());
        }
        if (pro.getId().equals(expediteurId)) {
            return enregistrer(pro, client, demande, request.contenu());
        }
        throw new AccessDeniedException(
                "Seuls le client et le professionnel de cette demande peuvent y écrire");
    }

    @Override
    public List<MessageResponse> messagesDeLaDemande(Long utilisateurId, Long demandeId) {
        Demande demande = chercherDemande(demandeId);
        boolean participant = demande.getClient().getId().equals(utilisateurId)
                || demande.getProfessionnel().getId().equals(utilisateurId);

        if (participant) {
            // Ouvrir la discussion = lire les messages reçus : ils deviennent "lus"
            messageRepository.findByDemandeIdAndDestinataireIdAndLuFalse(demandeId, utilisateurId)
                    .forEach(message -> message.setLu(true));
        } else if (!chercherUtilisateur(utilisateurId).aRole(NomRole.ADMINISTRATEUR)) {
            // Règle 8 : à part eux, seul l'administrateur peut lire (ex : pour un litige)
            throw new AccessDeniedException("Vous ne participez pas à cette demande");
        }

        return messageRepository.findByDemandeIdOrderByCreatedAtAscIdAsc(demandeId).stream()
                .map(messageMapper::toResponse)
                .toList();
    }

    // =====================================================================
    //                          POUR TOUT LE MONDE
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> mesConversations(Long utilisateurId) {
        // Une conversation = une personne + une demande (ou "question générale").
        // Les messages arrivent du plus récent au plus ancien : le 1er message rencontré
        // pour chaque conversation est donc son DERNIER message.
        Map<String, ConversationResponse> conversations = new LinkedHashMap<>();

        for (Message message : messageRepository.tousLesMessagesDe(utilisateurId)) {
            Utilisateur interlocuteur = message.getExpediteur().getId().equals(utilisateurId)
                    ? message.getDestinataire()
                    : message.getExpediteur();
            Demande demande = message.getDemande();
            String cle = interlocuteur.getId() + "-" + (demande != null ? demande.getId() : "general");

            if (!conversations.containsKey(cle)) {
                long nonLus = (demande != null)
                        ? messageRepository.findByDemandeIdAndDestinataireIdAndLuFalse(
                                demande.getId(), utilisateurId).size()
                        : messageRepository.findByExpediteurIdAndDestinataireIdAndDemandeIsNullAndLuFalse(
                                interlocuteur.getId(), utilisateurId).size();
                conversations.put(cle,
                        messageMapper.toConversation(interlocuteur, message, utilisateurId, nonLus));
            }
        }
        return new ArrayList<>(conversations.values());
    }

    @Override
    @Transactional(readOnly = true)
    public long nombreNonLus(Long utilisateurId) {
        return messageRepository.countByDestinataireIdAndLuFalse(utilisateurId);
    }

    // =====================================================================
    //                         MÉTHODES INTERNES
    // =====================================================================

    // Enregistre le message, puis prévient le destinataire (règle 6)
    private MessageResponse enregistrer(Utilisateur expediteur, Utilisateur destinataire,
                                        Demande demande, String contenu) {
        // Règle 6 (anti-spam) : on regarde AVANT d'enregistrer si le destinataire a déjà
        // des messages non lus dans CETTE conversation. Si oui, il est déjà prévenu.
        // (Dans une demande, les messages reçus viennent forcément de l'autre participant.)
        List<Message> dejaNonLus = (demande != null)
                ? messageRepository.findByDemandeIdAndDestinataireIdAndLuFalse(
                        demande.getId(), destinataire.getId())
                : messageRepository.findByExpediteurIdAndDestinataireIdAndDemandeIsNullAndLuFalse(
                        expediteur.getId(), destinataire.getId());
        boolean dejaPrevenu = !dejaNonLus.isEmpty();

        Message enregistre = messageRepository.save(
                messageMapper.toEntity(expediteur, destinataire, demande, contenu));

        if (!dejaPrevenu) {
            prevenirDestinataire(enregistre);
        }
        return messageMapper.toResponse(enregistre);
    }

    // Nouveau message : notification au destinataire, avec un extrait du texte.
    // Si le message parle d'une demande, la notification ouvre cette demande.
    private void prevenirDestinataire(Message message) {
        Utilisateur expediteur = message.getExpediteur();
        String nomExpediteur = expediteur.getPrenom() + " " + expediteur.getNom();

        String texte = message.getContenu().trim();
        String extrait = texte.length() > TAILLE_EXTRAIT
                ? texte.substring(0, TAILLE_EXTRAIT) + "..."
                : texte;

        Demande demande = message.getDemande();
        String texteNotification = (demande != null)
                ? "« " + extrait + " » (demande : " + demande.getService().getTitre() + ")"
                : "« " + extrait + " »";

        notificationService.notifier(
                message.getDestinataire(),
                TypeNotification.NOUVEAU_MESSAGE,
                "Nouveau message de " + nomExpediteur,
                texteNotification,
                demande != null ? demande.getId() : null);
    }

    // Règle 4 : un client n'écrit qu'à un professionnel validé et actif
    private void verifierProDisponible(Utilisateur pro) {
        if (pro.getStatutVerification() != StatutVerification.VALIDE
                || pro.getStatutCompte() != StatutCompte.ACTIF) {
            throw new BusinessException("Ce professionnel n'est pas disponible");
        }
    }

    // Règle 5 : un professionnel n'écrit qu'à un client qui l'a déjà contacté
    // (par un message ou par une demande) => pas de démarchage
    private void verifierClientAContacte(Utilisateur client, Utilisateur pro) {
        boolean aDejaEcrit = messageRepository
                .existsByExpediteurIdAndDestinataireId(client.getId(), pro.getId());
        boolean aFaitUneDemande = demandeRepository
                .existsByClientIdAndProfessionnelId(client.getId(), pro.getId());
        if (!aDejaEcrit && !aFaitUneDemande) {
            throw new BusinessException(
                    "Vous pouvez seulement écrire à un client qui vous a déjà contacté (message ou demande)");
        }
    }

    private Utilisateur chercherUtilisateur(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
    }

    private Demande chercherDemande(Long id) {
        return demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : " + id));
    }
}
