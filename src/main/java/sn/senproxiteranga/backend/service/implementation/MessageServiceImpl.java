package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sn.senproxiteranga.backend.domain.Demande;
import sn.senproxiteranga.backend.domain.Message;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.enums.NomRole;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.domain.enums.StatutVerification;
import sn.senproxiteranga.backend.domain.enums.TypeMessage;
import sn.senproxiteranga.backend.domain.enums.TypeNotification;
import sn.senproxiteranga.backend.dto.ConversationResponse;
import sn.senproxiteranga.backend.dto.MessageRequest;
import sn.senproxiteranga.backend.dto.MessageResponse;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.MessageMapper;
import sn.senproxiteranga.backend.domain.Notification;
import sn.senproxiteranga.backend.repository.DemandeRepository;
import sn.senproxiteranga.backend.repository.MessageRepository;
import sn.senproxiteranga.backend.repository.NotificationRepository;
import sn.senproxiteranga.backend.repository.UtilisateurRepository;
import sn.senproxiteranga.backend.service.MessageService;
import sn.senproxiteranga.backend.service.NotificationService;
import sn.senproxiteranga.backend.service.StockageFichierService;

import java.time.Duration;
import java.time.LocalDateTime;
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

    // Message vocal : 2 minutes au plus, 5 Mo au plus (2 minutes de voix ≈ 1 Mo)
    private static final int DUREE_MAX_AUDIO = 120;
    private static final long TAILLE_MAX_AUDIO = 5L * 1024 * 1024;

    // On peut supprimer son message pendant 24 h après l'envoi
    private static final Duration DELAI_SUPPRESSION = Duration.ofHours(24);

    // Formats de son acceptés -> extension du fichier rangé.
    // Chrome et Firefox enregistrent en "webm" ou "ogg", Safari (iPhone) en "mp4".
    private static final Map<String, String> FORMATS_AUDIO = Map.of(
            "audio/webm", "webm",
            "audio/ogg", "ogg",
            "audio/mp4", "m4a",
            "audio/mpeg", "mp3",
            "audio/aac", "aac");

    private final MessageRepository messageRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DemandeRepository demandeRepository;
    private final MessageMapper messageMapper;
    private final NotificationService notificationService;
    private final StockageFichierService stockageFichierService;
    private final NotificationRepository notificationRepository;

    // =====================================================================
    //               QUESTIONS GÉNÉRALES (messages sans demande)
    // =====================================================================

    @Override
    public MessageResponse envoyer(Long expediteurId, Long destinataireId, MessageRequest request) {
        Utilisateur expediteur = chercherUtilisateur(expediteurId);
        Utilisateur destinataire = chercherUtilisateur(destinataireId);
        verifierEchangeAutorise(expediteur, destinataire);

        // Règle 1 (texte non vide, 1000 caractères max) : vérifiée par @Valid sur MessageRequest
        return enregistrer(messageMapper.toEntity(expediteur, destinataire, null, request.contenu()));
    }

    @Override
    public MessageResponse envoyerAudio(Long expediteurId, Long destinataireId, MultipartFile fichier, int duree) {
        Utilisateur expediteur = chercherUtilisateur(expediteurId);
        Utilisateur destinataire = chercherUtilisateur(destinataireId);
        verifierEchangeAutorise(expediteur, destinataire);

        return enregistrer(nouveauVocal(expediteur, destinataire, null, fichier, duree));
    }

    @Override
    public List<MessageResponse> conversation(Long utilisateurId, Long interlocuteurId) {
        chercherUtilisateur(interlocuteurId);

        // Ouvrir la conversation = lire les messages reçus : ils deviennent "lus"
        messageRepository
                .findByExpediteurIdAndDestinataireIdAndDemandeIsNullAndLuFalse(interlocuteurId, utilisateurId)
                .forEach(message -> message.setLu(true));

        return messageRepository.conversationGenerale(utilisateurId, interlocuteurId).stream()
                .filter(message -> !message.estMasquePour(utilisateurId))   // « supprimés pour moi »
                .map(messageMapper::toResponse)
                .toList();
    }

    // =====================================================================
    //                      DISCUSSION D'UNE DEMANDE
    // =====================================================================

    @Override
    public MessageResponse envoyerDansDemande(Long expediteurId, Long demandeId, MessageRequest request) {
        Demande demande = chercherDemande(demandeId);
        Utilisateur expediteur = participant(demande, expediteurId);
        Utilisateur destinataire = autreParticipant(demande, expediteur);

        return enregistrer(messageMapper.toEntity(expediteur, destinataire, demande, request.contenu()));
    }

    @Override
    public MessageResponse envoyerAudioDansDemande(Long expediteurId, Long demandeId,
                                                   MultipartFile fichier, int duree) {
        Demande demande = chercherDemande(demandeId);
        Utilisateur expediteur = participant(demande, expediteurId);
        Utilisateur destinataire = autreParticipant(demande, expediteur);

        return enregistrer(nouveauVocal(expediteur, destinataire, demande, fichier, duree));
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

        // L'administrateur voit le texte d'origine des messages supprimés (preuve en cas de litige)
        boolean voirOriginal = !participant;
        // Un participant ne voit pas les messages qu'il a « supprimés pour lui » ;
        // l'administrateur voit tout
        return messageRepository.findByDemandeIdOrderByCreatedAtAscIdAsc(demandeId).stream()
                .filter(message -> !participant || !message.estMasquePour(utilisateurId))
                .map(message -> messageMapper.toResponse(message, voirOriginal))
                .toList();
    }

    // =====================================================================
    //                          POUR TOUT LE MONDE
    // =====================================================================

    @Override
    public MessageResponse supprimer(Long utilisateurId, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message introuvable : " + messageId));

        // Règle 10 : on ne supprime que SES messages
        if (!message.getExpediteur().getId().equals(utilisateurId)) {
            throw new AccessDeniedException("Vous pouvez supprimer uniquement vos propres messages");
        }
        if (message.isSupprime()) {
            throw new BusinessException("Ce message est déjà supprimé");
        }
        // Règle 11 : pendant 24 h après l'envoi seulement
        if (message.getCreatedAt().plus(DELAI_SUPPRESSION).isBefore(LocalDateTime.now())) {
            throw new BusinessException("Un message ne peut plus être supprimé 24 h après son envoi");
        }

        // Un vocal : on efface vraiment le son du dossier "uploads"
        if (message.getType() == TypeMessage.AUDIO && message.getAudioNom() != null) {
            stockageFichierService.supprimer(message.getAudioNom());
            message.setAudioNom(null);
        }
        // La notification reçue par l'autre montrait un extrait du texte : on le masque aussi
        masquerNotification(message);

        // Le texte reste dans la base (l'administrateur peut le voir en cas de litige)
        message.setSupprime(true);
        message.setDateSuppression(LocalDateTime.now());
        return messageMapper.toResponse(message);
    }

    @Override
    public void masquerPourMoi(Long utilisateurId, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message introuvable : " + messageId));

        // Règle 12 : seulement un message de MES discussions, et seulement pour moi
        if (message.getExpediteur().getId().equals(utilisateurId)) {
            message.setMasquePourExpediteur(true);
        } else if (message.getDestinataire().getId().equals(utilisateurId)) {
            message.setMasquePourDestinataire(true);
            message.setLu(true);   // un message caché ne doit plus compter comme « non lu »
        } else {
            throw new AccessDeniedException("Ce message ne fait pas partie de vos discussions");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FichierAudio chargerAudio(Long utilisateurId, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .filter(m -> m.getType() == TypeMessage.AUDIO && m.getAudioNom() != null && !m.isSupprime())
                .orElseThrow(() -> new ResourceNotFoundException("Message vocal introuvable : " + messageId));

        // Règle 9 : seuls l'expéditeur et le destinataire écoutent le vocal
        // (et l'administrateur, comme pour lire une discussion en cas de litige)
        boolean concerne = message.getExpediteur().getId().equals(utilisateurId)
                || message.getDestinataire().getId().equals(utilisateurId);
        if (!concerne && !chercherUtilisateur(utilisateurId).aRole(NomRole.ADMINISTRATEUR)) {
            throw new AccessDeniedException("Ce message vocal ne vous est pas destiné");
        }

        return new FichierAudio(stockageFichierService.charger(message.getAudioNom()), message.getAudioType());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> mesConversations(Long utilisateurId) {
        // Une conversation = une personne + une demande (ou "question générale").
        // Les messages arrivent du plus récent au plus ancien : le 1er message rencontré
        // pour chaque conversation est donc son DERNIER message.
        Map<String, ConversationResponse> conversations = new LinkedHashMap<>();

        for (Message message : messageRepository.tousLesMessagesDe(utilisateurId)) {
            // Les messages « supprimés pour moi » ne comptent pas (ni comme dernier message)
            if (message.estMasquePour(utilisateurId)) {
                continue;
            }
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

    // Règles 2 à 5 pour une question générale (sans demande)
    private void verifierEchangeAutorise(Utilisateur expediteur, Utilisateur destinataire) {
        // Règle 2 : on ne s'écrit pas à soi-même
        if (expediteur.getId().equals(destinataire.getId())) {
            throw new BusinessException("Vous ne pouvez pas vous écrire à vous-même");
        }
        // Règle 3 : seulement entre un client et un professionnel
        if (expediteur.aRole(NomRole.CLIENT) && destinataire.aRole(NomRole.PROFESSIONNEL)) {
            verifierProDisponible(destinataire);                              // Règle 4
        } else if (expediteur.aRole(NomRole.PROFESSIONNEL) && destinataire.aRole(NomRole.CLIENT)) {
            verifierClientAContacte(destinataire, expediteur);                 // Règle 5
        } else {
            throw new BusinessException(
                    "Les messages s'échangent uniquement entre un client et un professionnel");
        }
    }

    // Règle 7 : seuls le client et le professionnel de la demande y écrivent
    private Utilisateur participant(Demande demande, Long utilisateurId) {
        if (demande.getClient().getId().equals(utilisateurId)) {
            return demande.getClient();
        }
        if (demande.getProfessionnel().getId().equals(utilisateurId)) {
            return demande.getProfessionnel();
        }
        throw new AccessDeniedException(
                "Seuls le client et le professionnel de cette demande peuvent y écrire");
    }

    // Dans une demande, le message va toujours à "l'autre"
    private Utilisateur autreParticipant(Demande demande, Utilisateur expediteur) {
        return demande.getClient().getId().equals(expediteur.getId())
                ? demande.getProfessionnel()
                : demande.getClient();
    }

    // Vérifie le son envoyé, le range dans "uploads" et prépare le message vocal
    private Message nouveauVocal(Utilisateur expediteur, Utilisateur destinataire, Demande demande,
                                 MultipartFile fichier, int duree) {
        if (fichier == null || fichier.isEmpty()) {
            throw new BusinessException("Le message vocal est vide");
        }
        if (fichier.getSize() > TAILLE_MAX_AUDIO) {
            throw new BusinessException("Le message vocal est trop lourd (5 Mo maximum)");
        }
        if (duree < 1 || duree > DUREE_MAX_AUDIO) {
            throw new BusinessException("Un message vocal dure entre 1 seconde et 2 minutes");
        }
        // Ex : "audio/webm;codecs=opus" -> "audio/webm"
        String format = fichier.getContentType() == null ? ""
                : fichier.getContentType().split(";")[0].trim().toLowerCase();
        String extension = FORMATS_AUDIO.get(format);
        if (extension == null) {
            throw new BusinessException("Format de son non accepté : " + format);
        }

        String nomStocke = stockageFichierService.enregistrer(fichier, extension);
        return messageMapper.toAudioEntity(expediteur, destinataire, demande, nomStocke, format, duree);
    }

    // Enregistre le message (écrit ou vocal), puis prévient le destinataire (règle 6)
    private MessageResponse enregistrer(Message nouveau) {
        Utilisateur expediteur = nouveau.getExpediteur();
        Utilisateur destinataire = nouveau.getDestinataire();
        Demande demande = nouveau.getDemande();

        // Règle 6 (anti-spam) : on regarde AVANT d'enregistrer si le destinataire a déjà
        // des messages non lus dans CETTE conversation. Si oui, il est déjà prévenu.
        // (Dans une demande, les messages reçus viennent forcément de l'autre participant.)
        List<Message> dejaNonLus = (demande != null)
                ? messageRepository.findByDemandeIdAndDestinataireIdAndLuFalse(
                        demande.getId(), destinataire.getId())
                : messageRepository.findByExpediteurIdAndDestinataireIdAndDemandeIsNullAndLuFalse(
                        expediteur.getId(), destinataire.getId());
        boolean dejaPrevenu = !dejaNonLus.isEmpty();

        Message enregistre = messageRepository.save(nouveau);

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

        String extrait = extrait(message);

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

    // Le début du texte, affiché dans la notification
    private String extrait(Message message) {
        String texte = message.getContenu().trim();
        return texte.length() > TAILLE_EXTRAIT ? texte.substring(0, TAILLE_EXTRAIT) + "..." : texte;
    }

    // Message supprimé : la notification « Nouveau message » qui citait son texte
    // affiche maintenant « Ce message a été supprimé »
    private void masquerNotification(Message message) {
        String debut = "« " + extrait(message) + " »";
        for (Notification notification : notificationRepository
                .findByDestinataireIdOrderByCreatedAtDescIdDesc(message.getDestinataire().getId())) {
            if (notification.getType() == TypeNotification.NOUVEAU_MESSAGE
                    && notification.getMessage() != null
                    && notification.getMessage().startsWith(debut)) {
                notification.setMessage("« " + MessageMapper.TEXTE_SUPPRIME + " »"
                        + notification.getMessage().substring(debut.length()));
            }
        }
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
