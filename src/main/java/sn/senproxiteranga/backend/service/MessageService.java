package sn.senproxiteranga.backend.service;

import sn.senproxiteranga.backend.dto.ConversationResponse;
import sn.senproxiteranga.backend.dto.MessageRequest;
import sn.senproxiteranga.backend.dto.MessageResponse;

import java.util.List;

public interface MessageService {

    // ---------- Questions générales (sans demande) ----------

    // Écrire à quelqu'un sans parler d'une demande précise
    MessageResponse envoyer(Long expediteurId, Long destinataireId, MessageRequest request);

    // Les questions générales avec un interlocuteur (celles reçues deviennent "lues")
    List<MessageResponse> conversation(Long utilisateurId, Long interlocuteurId);

    // ---------- Discussion d'une demande ----------

    // Écrire dans la discussion d'une demande : le message va à "l'autre"
    // (le pro si j'écris en tant que client, le client si j'écris en tant que pro)
    MessageResponse envoyerDansDemande(Long expediteurId, Long demandeId, MessageRequest request);

    // Les messages d'une demande (ceux reçus deviennent "lus").
    // L'administrateur peut les lire (ex : pour trancher un litige), sans les marquer lus.
    List<MessageResponse> messagesDeLaDemande(Long utilisateurId, Long demandeId);

    // ---------- Pour tout le monde ----------

    // La liste des conversations de l'utilisateur, la plus récente d'abord
    // (une par demande, plus une "question générale" par personne s'il y en a)
    List<ConversationResponse> mesConversations(Long utilisateurId);

    // Nombre total de messages non lus (pastille rouge)
    long nombreNonLus(Long utilisateurId);
}
