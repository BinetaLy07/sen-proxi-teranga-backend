package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.senproxiteranga.backend.domain.Message;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.enums.NomRole;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.domain.enums.StatutVerification;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DemandeRepository demandeRepository;
    private final MessageMapper messageMapper;

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
        Message message = messageMapper.toEntity(expediteur, destinataire, request.contenu());
        return messageMapper.toResponse(messageRepository.save(message));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> mesConversations(Long utilisateurId) {
        // Les messages arrivent du plus récent au plus ancien : le 1er message rencontré
        // avec chaque interlocuteur est donc le DERNIER de cette conversation
        Map<Long, ConversationResponse> conversations = new LinkedHashMap<>();

        for (Message message : messageRepository.tousLesMessagesDe(utilisateurId)) {
            Utilisateur interlocuteur = message.getExpediteur().getId().equals(utilisateurId)
                    ? message.getDestinataire()
                    : message.getExpediteur();

            if (!conversations.containsKey(interlocuteur.getId())) {
                long nonLus = messageRepository
                        .findByExpediteurIdAndDestinataireIdAndLuFalse(interlocuteur.getId(), utilisateurId)
                        .size();
                conversations.put(interlocuteur.getId(),
                        messageMapper.toConversation(interlocuteur, message, utilisateurId, nonLus));
            }
        }
        return new ArrayList<>(conversations.values());
    }

    @Override
    public List<MessageResponse> conversation(Long utilisateurId, Long interlocuteurId) {
        chercherUtilisateur(interlocuteurId);

        // Ouvrir la conversation = lire les messages reçus : ils deviennent "lus"
        messageRepository.findByExpediteurIdAndDestinataireIdAndLuFalse(interlocuteurId, utilisateurId)
                .forEach(message -> message.setLu(true));

        return messageRepository.conversation(utilisateurId, interlocuteurId).stream()
                .map(messageMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long nombreNonLus(Long utilisateurId) {
        return messageRepository.countByDestinataireIdAndLuFalse(utilisateurId);
    }

    // ---------- Méthodes internes ----------

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
}