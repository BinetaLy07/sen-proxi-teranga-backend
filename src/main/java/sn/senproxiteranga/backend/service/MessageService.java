package sn.senproxiteranga.backend.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import sn.senproxiteranga.backend.dto.ConversationResponse;
import sn.senproxiteranga.backend.dto.MessageRequest;
import sn.senproxiteranga.backend.dto.MessageResponse;

import java.util.List;

public interface MessageService {

    // Le son d'un message vocal + son format (ex : "audio/webm"), pour le lire
    record FichierAudio(Resource ressource, String contentType) {
    }

    // ---------- Questions générales (sans demande) ----------

    // Écrire à quelqu'un sans parler d'une demande précise
    MessageResponse envoyer(Long expediteurId, Long destinataireId, MessageRequest request);

    // Envoyer un message VOCAL à quelqu'un, sans parler d'une demande précise
    MessageResponse envoyerAudio(Long expediteurId, Long destinataireId, MultipartFile fichier, int duree);

    // Les questions générales avec un interlocuteur (celles reçues deviennent "lues")
    List<MessageResponse> conversation(Long utilisateurId, Long interlocuteurId);

    // ---------- Discussion d'une demande ----------

    // Écrire dans la discussion d'une demande : le message va à "l'autre"
    // (le pro si j'écris en tant que client, le client si j'écris en tant que pro)
    MessageResponse envoyerDansDemande(Long expediteurId, Long demandeId, MessageRequest request);

    // Envoyer un message VOCAL dans la discussion d'une demande
    MessageResponse envoyerAudioDansDemande(Long expediteurId, Long demandeId, MultipartFile fichier, int duree);

    // Les messages d'une demande (ceux reçus deviennent "lus").
    // L'administrateur peut les lire (ex : pour trancher un litige), sans les marquer lus.
    List<MessageResponse> messagesDeLaDemande(Long utilisateurId, Long demandeId);

    // ---------- Pour tout le monde ----------

    // Supprimer MON message, pendant les 24 h après l'envoi (comme WhatsApp)
    MessageResponse supprimer(Long utilisateurId, Long messageId);

    // « Supprimer pour moi » : n'importe quel message de mes discussions (le mien, celui
    // de l'autre, ou une trace « supprimé ») disparaît seulement de MON écran
    void masquerPourMoi(Long utilisateurId, Long messageId);

    // Écouter un message vocal : seulement l'expéditeur, le destinataire ou l'administrateur
    FichierAudio chargerAudio(Long utilisateurId, Long messageId);

    // La liste des conversations de l'utilisateur, la plus récente d'abord
    // (une par demande, plus une "question générale" par personne s'il y en a)
    List<ConversationResponse> mesConversations(Long utilisateurId);

    // Nombre total de messages non lus (pastille rouge)
    long nombreNonLus(Long utilisateurId);
}
