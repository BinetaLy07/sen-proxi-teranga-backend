package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.senproxiteranga.backend.domain.Demande;
import sn.senproxiteranga.backend.domain.RendezVous;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.enums.NomRole;
import sn.senproxiteranga.backend.domain.enums.StatutDemande;
import sn.senproxiteranga.backend.domain.enums.StatutRendezVous;
import sn.senproxiteranga.backend.domain.enums.TypeNotification;
import sn.senproxiteranga.backend.dto.RendezVousRequest;
import sn.senproxiteranga.backend.dto.RendezVousResponse;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.RendezVousMapper;
import sn.senproxiteranga.backend.repository.DemandeRepository;
import sn.senproxiteranga.backend.repository.RendezVousRepository;
import sn.senproxiteranga.backend.service.NotificationService;
import sn.senproxiteranga.backend.service.RendezVousService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class RendezVousServiceImpl implements RendezVousService {

    // Taille maximale des colonnes "motif" (rendez-vous) et "message" (notification)
    private static final int TAILLE_MAX_TEXTE = 500;

    // Pour écrire une date lisible dans les notifications : "jeudi 09/10 à 10h00"
    private static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("EEEE dd/MM 'à' HH'h'mm", Locale.FRENCH);

    private final RendezVousRepository rendezVousRepository;
    private final DemandeRepository demandeRepository;
    private final RendezVousMapper rendezVousMapper;
    private final NotificationService notificationService;

    // =====================================================================
    //                        ACTIONS DU CLIENT
    // =====================================================================

    @Override
    public RendezVousResponse proposerParClient(Long clientId, Long demandeId, RendezVousRequest request) {
        Demande demande = chercherDemandeDuClient(clientId, demandeId);
        return proposer(demande, request, NomRole.CLIENT);
    }

    @Override
    public RendezVousResponse accepterParClient(Long clientId, Long demandeId) {
        Demande demande = chercherDemandeDuClient(clientId, demandeId);
        return accepter(demande, NomRole.CLIENT);
    }

    @Override
    public RendezVousResponse reporterParClient(Long clientId, Long demandeId, String motif) {
        Demande demande = chercherDemandeDuClient(clientId, demandeId);
        return reporter(demande, NomRole.CLIENT, motif.trim());
    }

    @Override
    public RendezVousResponse confirmerFinTravaux(Long clientId, Long demandeId) {
        Demande demande = chercherDemandeDuClient(clientId, demandeId);

        // Règle 7 : le client confirme seulement des travaux terminés
        if (demande.getStatut() != StatutDemande.TERMINEE) {
            throw new BusinessException(
                    "Vous ne pouvez confirmer que des travaux terminés (statut actuel : "
                            + demande.getStatut() + ")");
        }

        demande.setStatut(StatutDemande.CONFIRMEE);
        return rendezVousMapper.toResponse(chercherActuel(demandeId));
    }

    // =====================================================================
    //                    ACTIONS DU PROFESSIONNEL
    // =====================================================================

    @Override
    public RendezVousResponse proposerParProfessionnel(Long professionnelId, Long demandeId,
                                                       RendezVousRequest request) {
        Demande demande = chercherDemandeDuPro(professionnelId, demandeId);
        return proposer(demande, request, NomRole.PROFESSIONNEL);
    }

    @Override
    public RendezVousResponse accepterParProfessionnel(Long professionnelId, Long demandeId) {
        Demande demande = chercherDemandeDuPro(professionnelId, demandeId);
        return accepter(demande, NomRole.PROFESSIONNEL);
    }

    @Override
    public RendezVousResponse reporterParProfessionnel(Long professionnelId, Long demandeId, String motif) {
        Demande demande = chercherDemandeDuPro(professionnelId, demandeId);
        return reporter(demande, NomRole.PROFESSIONNEL, motif.trim());
    }

    @Override
    public RendezVousResponse commencer(Long professionnelId, Long demandeId) {
        Demande demande = chercherDemandeDuPro(professionnelId, demandeId);

        // Règle 5 : on ne commence qu'un rendez-vous planifié
        if (demande.getStatut() != StatutDemande.PLANIFIEE) {
            throw new BusinessException(
                    "Les travaux ne peuvent commencer que pour un rendez-vous planifié (statut actuel : "
                            + demande.getStatut() + ")");
        }

        RendezVous rendezVous = chercherActuel(demandeId);
        rendezVous.setDateDebutTravaux(LocalDateTime.now());
        demande.setStatut(StatutDemande.EN_COURS);

        return rendezVousMapper.toResponse(rendezVous);
    }

    @Override
    public RendezVousResponse terminer(Long professionnelId, Long demandeId) {
        Demande demande = chercherDemandeDuPro(professionnelId, demandeId);

        // Règle 6 : on ne termine que des travaux en cours
        if (demande.getStatut() != StatutDemande.EN_COURS) {
            throw new BusinessException(
                    "Les travaux ne peuvent être terminés que s'ils sont en cours (statut actuel : "
                            + demande.getStatut() + ")");
        }

        RendezVous rendezVous = chercherActuel(demandeId);
        rendezVous.setDateFinTravaux(LocalDateTime.now());
        demande.setStatut(StatutDemande.TERMINEE);

        return rendezVousMapper.toResponse(rendezVous);
    }

    // =====================================================================
    //                          CONSULTATION
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public RendezVousResponse trouverActuel(Long demandeId) {
        verifierDemandeExiste(demandeId);
        return rendezVousMapper.toResponse(chercherActuel(demandeId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousResponse> historique(Long demandeId) {
        verifierDemandeExiste(demandeId);
        return rendezVousRepository.findByDemandeIdOrderByIdAsc(demandeId).stream()
                .map(rendezVousMapper::toResponse)
                .toList();
    }

    // =====================================================================
    //          RÈGLES COMMUNES AU CLIENT ET AU PROFESSIONNEL
    // =====================================================================

    /**
     * Proposer une date (client ou professionnel).
     * Règle 1 : seulement après l'acceptation du devis.
     * Règle 2 : la première date vient toujours du client.
     * Règle 3 : celui qui a proposé attend ; il ne propose pas une 2e fois.
     * Si l'autre attendait une réponse, sa date est remplacée (elle passe à REFUSE).
     */
    private RendezVousResponse proposer(Demande demande, RendezVousRequest request, NomRole auteur) {
        // Règle 1
        if (demande.getStatut() != StatutDemande.DEVIS_ACCEPTE) {
            throw new BusinessException(
                    "Une date ne peut être proposée qu'après l'acceptation du devis (statut actuel : "
                            + demande.getStatut() + ")");
        }

        RendezVous enAttente = chercherEnAttente(demande.getId());

        if (enAttente == null && auteur == NomRole.PROFESSIONNEL) {
            // Règle 2
            throw new BusinessException(
                    "C'est le client qui choisit d'abord la date : vous pourrez ensuite "
                            + "l'accepter ou proposer une autre date");
        }
        if (enAttente != null && enAttente.getProposePar() == auteur) {
            // Règle 3
            throw new BusinessException(
                    "Votre date du " + lisible(enAttente.getDateHeure())
                            + " attend déjà une réponse");
        }
        if (enAttente != null) {
            // La date de l'autre ne convient pas : elle est remplacée par la nouvelle
            enAttente.setStatut(StatutRendezVous.REFUSE);
            enAttente.setMotif(auteur == NomRole.CLIENT
                    ? "Le client a proposé une autre date"
                    : "Le professionnel a proposé une autre date");
        }

        RendezVous nouveau = rendezVousRepository.save(
                rendezVousMapper.toEntity(demande, request, auteur));

        // On prévient l'autre : c'est à lui de répondre
        String nom = nomDe(demande, auteur);
        String titre;
        String message;
        if (enAttente == null) {
            titre = "Date de rendez-vous proposée";
            message = nom + " propose le " + lisible(nouveau.getDateHeure())
                    + " pour : " + demande.getService().getTitre()
                    + ". Acceptez cette date ou proposez-en une autre.";
        } else {
            titre = "Autre date proposée";
            message = nom + " ne peut pas le " + lisible(enAttente.getDateHeure())
                    + " et propose le " + lisible(nouveau.getDateHeure())
                    + ". Acceptez cette date ou proposez-en une autre.";
        }
        prevenir(demande, autre(auteur), TypeNotification.RENDEZ_VOUS_PROPOSE, titre, message);

        return rendezVousMapper.toResponse(nouveau);
    }

    /**
     * Accepter la date proposée par l'AUTRE (client ou professionnel).
     * Règle 3 : on ne répond pas à sa propre date.
     * Règle 4 : on n'accepte pas une date déjà passée.
     */
    private RendezVousResponse accepter(Demande demande, NomRole qui) {
        RendezVous enAttente = chercherEnAttente(demande.getId());

        // Règle 3
        if (demande.getStatut() != StatutDemande.DEVIS_ACCEPTE
                || enAttente == null
                || enAttente.getProposePar() == qui) {
            throw new BusinessException("Aucune date n'attend votre réponse pour cette demande");
        }

        // Règle 4
        if (enAttente.getDateHeure().isBefore(LocalDateTime.now())) {
            throw new BusinessException(
                    "Cette date est déjà passée : proposez une autre date");
        }

        enAttente.setStatut(StatutRendezVous.ACCEPTE);
        demande.setStatut(StatutDemande.PLANIFIEE);

        // On prévient celui qui avait proposé : sa date est acceptée
        prevenir(demande, enAttente.getProposePar(), TypeNotification.RENDEZ_VOUS_ACCEPTE,
                "Rendez-vous confirmé",
                nomDe(demande, qui) + " a accepté le rendez-vous du "
                        + lisible(enAttente.getDateHeure())
                        + " pour : " + demande.getService().getTitre() + ".");

        return rendezVousMapper.toResponse(enAttente);
    }

    /**
     * Règle 8 : report commun au client et au professionnel.
     * Possible seulement quand le rendez-vous est accepté et que les travaux n'ont pas commencé.
     * Ensuite, le client choisit une nouvelle date (Règle 2).
     */
    private RendezVousResponse reporter(Demande demande, NomRole qui, String motif) {
        if (demande.getStatut() != StatutDemande.PLANIFIEE) {
            throw new BusinessException(
                    "Seul un rendez-vous planifié peut être reporté, avant le début des travaux (statut actuel : "
                            + demande.getStatut() + ")");
        }

        RendezVous rendezVous = chercherActuel(demande.getId());
        rendezVous.setStatut(StatutRendezVous.REPORTE);
        rendezVous.setMotif(limiter((qui == NomRole.CLIENT
                ? "Report demandé par le client : "
                : "Report demandé par le professionnel : ") + motif));

        // Retour à DEVIS_ACCEPTE : le client doit proposer une nouvelle date
        demande.setStatut(StatutDemande.DEVIS_ACCEPTE);

        // On prévient l'autre
        String suite = (qui == NomRole.CLIENT)
                ? "Le client va proposer une nouvelle date."
                : "Choisissez une nouvelle date.";
        prevenir(demande, autre(qui), TypeNotification.RENDEZ_VOUS_REPORTE,
                "Rendez-vous reporté",
                nomDe(demande, qui) + " a reporté le rendez-vous du "
                        + lisible(rendezVous.getDateHeure()) + " (« " + motif + " »). " + suite);

        return rendezVousMapper.toResponse(rendezVous);
    }

    // =====================================================================
    //                       MÉTHODES INTERNES
    // =====================================================================

    // La date qui attend une réponse (statut PROPOSE), ou null s'il n'y en a pas
    private RendezVous chercherEnAttente(Long demandeId) {
        return rendezVousRepository.findFirstByDemandeIdOrderByIdDesc(demandeId)
                .filter(rdv -> rdv.getStatut() == StatutRendezVous.PROPOSE)
                .orElse(null);
    }

    // Envoie une notification au client ou au professionnel de la demande
    private void prevenir(Demande demande, NomRole destinataire, TypeNotification type,
                          String titre, String message) {
        Utilisateur personne = (destinataire == NomRole.CLIENT)
                ? demande.getClient()
                : demande.getProfessionnel();
        notificationService.notifier(personne, type, titre, limiter(message), demande.getId());
    }

    // "Awa Diop" ou "Bineta Ly"
    private String nomDe(Demande demande, NomRole role) {
        Utilisateur personne = (role == NomRole.CLIENT)
                ? demande.getClient()
                : demande.getProfessionnel();
        return personne.getPrenom() + " " + personne.getNom();
    }

    // Le client <-> le professionnel
    private NomRole autre(NomRole role) {
        return (role == NomRole.CLIENT) ? NomRole.PROFESSIONNEL : NomRole.CLIENT;
    }

    // 2026-10-09T10:00 -> "jeudi 09/10 à 10h00"
    private String lisible(LocalDateTime dateHeure) {
        return dateHeure.format(FORMAT_DATE);
    }

    // Coupe un texte s'il dépasse la taille de la colonne
    private String limiter(String texte) {
        return texte.length() > TAILLE_MAX_TEXTE ? texte.substring(0, TAILLE_MAX_TEXTE) : texte;
    }

    private RendezVous chercherActuel(Long demandeId) {
        return rendezVousRepository.findFirstByDemandeIdOrderByIdDesc(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucun rendez-vous n'a encore été proposé pour la demande " + demandeId));
    }

    private Demande chercherDemandeDuPro(Long professionnelId, Long demandeId) {
        return demandeRepository.findByIdAndProfessionnelId(demandeId, professionnelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Demande " + demandeId + " introuvable pour ce professionnel"));
    }

    private Demande chercherDemandeDuClient(Long clientId, Long demandeId) {
        return demandeRepository.findByIdAndClientId(demandeId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Demande " + demandeId + " introuvable pour ce client"));
    }

    private void verifierDemandeExiste(Long demandeId) {
        if (!demandeRepository.existsById(demandeId)) {
            throw new ResourceNotFoundException("Demande introuvable : " + demandeId);
        }
    }
}
