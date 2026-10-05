package sn.senproxiteranga.backend.service;

import sn.senproxiteranga.backend.dto.ConversationResponse;
import sn.senproxiteranga.backend.dto.MessageRequest;
import sn.senproxiteranga.backend.dto.MessageResponse;

import java.util.List;

public interface MessageService {

    MessageResponse envoyer(Long expediteurId, Long destinataireId, MessageRequest request);

    // La liste des conversations de l'utilisateur, la plus récente d'abord
    List<ConversationResponse> mesConversations(Long utilisateurId);

    // Tous les messages avec un interlocuteur (et ceux reçus deviennent "lus")
    List<MessageResponse> conversation(Long utilisateurId, Long interlocuteurId);

    // Nombre total de messages non lus (pastille rouge)
    long nombreNonLus(Long utilisateurId);
}