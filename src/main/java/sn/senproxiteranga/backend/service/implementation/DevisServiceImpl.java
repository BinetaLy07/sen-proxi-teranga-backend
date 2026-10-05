package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.senproxiteranga.backend.domain.Demande;
import sn.senproxiteranga.backend.domain.Devis;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.enums.StatutDemande;
import sn.senproxiteranga.backend.domain.enums.StatutDevis;
import sn.senproxiteranga.backend.domain.enums.TypeLigneDevis;
import sn.senproxiteranga.backend.domain.enums.TypeNotification;
import sn.senproxiteranga.backend.dto.DevisRequest;
import sn.senproxiteranga.backend.dto.DevisResponse;
import sn.senproxiteranga.backend.dto.LigneDevisRequest;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.DevisMapper;
import sn.senproxiteranga.backend.repository.DemandeRepository;
import sn.senproxiteranga.backend.repository.DevisRepository;
import sn.senproxiteranga.backend.service.DevisService;
import sn.senproxiteranga.backend.service.NotificationService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DevisServiceImpl implements DevisService {

    // Le client peut demander 2 révisions au maximum (donc 3 versions au plus)
    private static final int MAX_REVISIONS = 2;

    private final DevisRepository devisRepository;
    private final DemandeRepository demandeRepository;
    private final DevisMapper devisMapper;
    private final NotificationService notificationService;

    // =====================================================================
    //                    ACTIONS DU PROFESSIONNEL
    // =====================================================================

    @Override
    public DevisResponse envoyer(Long professionnelId, Long demandeId, DevisRequest request) {
        Demande demande = chercherDemandeDuPro(professionnelId, demandeId);

        // Règle 1 : un devis seulement pour une demande acceptée
        if (demande.getStatut() != StatutDemande.ACCEPTEE) {
            throw new BusinessException(
                    "Un devis ne peut être envoyé que pour une demande acceptée (statut actuel : "
                            + demande.getStatut() + ")");
        }

        // Règle 2 : un seul "premier" devis par demande
        if (devisRepository.existsByDemandeId(demandeId)) {
            throw new BusinessException("Un devis a déjà été envoyé pour cette demande");
        }

        // Règle 3 : main-d'œuvre obligatoire et unique, un seul déplacement
        verifierLignes(request);

        Devis devis = new Devis();
        devis.setDemande(demande);
        devis.setNumeroVersion(1);
        devisMapper.ajouterLignes(devis, request);   // calcule les montants et le total

        Devis enregistre = devisRepository.save(devis);

        // La demande avance dans son cycle de vie
        demande.setStatut(StatutDemande.DEVIS_ENVOYE);

        prevenirClientDevis(enregistre);
        return devisMapper.toResponse(enregistre);
    }

    @Override
    public DevisResponse reviser(Long professionnelId, Long demandeId, DevisRequest request) {
        chercherDemandeDuPro(professionnelId, demandeId);
        Devis actuel = chercherDevisActuel(demandeId);

        // On ne peut réviser que si le client l'a demandé
        if (actuel.getStatut() != StatutDevis.REVISION_DEMANDEE) {
            throw new BusinessException(
                    "Le client n'a pas demandé de révision pour ce devis (statut : "
                            + actuel.getStatut() + ")");
        }

        verifierLignes(request);

        // L'ancienne version est gardée pour l'historique
        actuel.setStatut(StatutDevis.REMPLACE);

        // La nouvelle version
        Devis nouveau = new Devis();
        nouveau.setDemande(actuel.getDemande());
        nouveau.setNumeroVersion(actuel.getNumeroVersion() + 1);
        devisMapper.ajouterLignes(nouveau, request);

        Devis enregistre = devisRepository.save(nouveau);
        prevenirClientDevis(enregistre);
        return devisMapper.toResponse(enregistre);
    }

    // =====================================================================
    //                        ACTIONS DU CLIENT
    // =====================================================================

    @Override
    public DevisResponse accepter(Long clientId, Long demandeId) {
        Demande demande = chercherDemandeDuClient(clientId, demandeId);
        Devis actuel = chercherDevisActuel(demandeId);
        verifierEnAttenteDeReponse(actuel);

        actuel.setStatut(StatutDevis.ACCEPTE);
        demande.setStatut(StatutDemande.DEVIS_ACCEPTE);

        return devisMapper.toResponse(actuel);
    }

    @Override
    public DevisResponse refuser(Long clientId, Long demandeId, String motif) {
        Demande demande = chercherDemandeDuClient(clientId, demandeId);
        Devis actuel = chercherDevisActuel(demandeId);
        verifierEnAttenteDeReponse(actuel);

        actuel.setStatut(StatutDevis.REFUSE);
        actuel.setMotifRefus(motif.trim());

        // Devis refusé => la demande est annulée (le détail du motif reste sur le devis)
        demande.setStatut(StatutDemande.ANNULEE);
        demande.setMotifAnnulation("Devis refusé");

        return devisMapper.toResponse(actuel);
    }

    @Override
    public DevisResponse demanderRevision(Long clientId, Long demandeId, String motif) {
        chercherDemandeDuClient(clientId, demandeId);
        Devis actuel = chercherDevisActuel(demandeId);
        verifierEnAttenteDeReponse(actuel);

        // Version 1 => 0 révision faite, version 3 => 2 révisions faites
        int revisionsFaites = actuel.getNumeroVersion() - 1;
        if (revisionsFaites >= MAX_REVISIONS) {
            throw new BusinessException(
                    "Vous avez déjà demandé " + MAX_REVISIONS
                            + " révisions : vous pouvez seulement accepter ou refuser ce devis");
        }

        actuel.setStatut(StatutDevis.REVISION_DEMANDEE);
        actuel.setMotifRevision(motif.trim());

        return devisMapper.toResponse(actuel);
    }

    // =====================================================================
    //                          CONSULTATION
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public DevisResponse trouverActuel(Long demandeId) {
        verifierDemandeExiste(demandeId);
        return devisMapper.toResponse(chercherDevisActuel(demandeId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DevisResponse> historique(Long demandeId) {
        verifierDemandeExiste(demandeId);
        return devisRepository.findByDemandeIdOrderByNumeroVersionAsc(demandeId).stream()
                .map(devisMapper::toResponse)
                .toList();
    }

    // =====================================================================
    //                          NOTIFICATIONS
    // =====================================================================

    // Nouveau devis (1re version ou révision) : on prévient le client avec le montant
    private void prevenirClientDevis(Devis devis) {
        Demande demande = devis.getDemande();
        Utilisateur pro = demande.getProfessionnel();
        String nomPro = pro.getPrenom() + " " + pro.getNom();
        String montant = String.format("%.0f F CFA", devis.getMontantTotal());

        String titre = (devis.getNumeroVersion() == 1)
                ? "Devis reçu"
                : "Nouveau devis (version " + devis.getNumeroVersion() + ")";
        String message = nomPro + " vous a envoyé un devis de " + montant
                + " pour : " + demande.getService().getTitre()
                + ". Ouvrez la demande pour l'accepter, le refuser ou demander une révision.";

        notificationService.notifier(
                demande.getClient(), TypeNotification.DEVIS_RECU, titre, message, demande.getId());
    }

    // =====================================================================
    //                     MÉTHODES INTERNES (règles)
    // =====================================================================

    /**
     * Vérifie les lignes du devis :
     * - exactement une ligne de main-d'œuvre
     * - au plus une ligne de déplacement
     */
    private void verifierLignes(DevisRequest request) {
        long nbMainOeuvre = compterLignes(request, TypeLigneDevis.MAIN_OEUVRE);
        long nbDeplacement = compterLignes(request, TypeLigneDevis.DEPLACEMENT);

        if (nbMainOeuvre == 0) {
            throw new BusinessException("La main-d'œuvre est obligatoire dans un devis");
        }
        if (nbMainOeuvre > 1) {
            throw new BusinessException("Un devis ne peut contenir qu'une seule ligne de main-d'œuvre");
        }
        if (nbDeplacement > 1) {
            throw new BusinessException("Un devis ne peut contenir qu'une seule ligne de déplacement");
        }
    }

    private long compterLignes(DevisRequest request, TypeLigneDevis type) {
        return request.lignes().stream()
                .map(LigneDevisRequest::type)
                .filter(t -> t == type)
                .count();
    }

    /**
     * Le client ne peut répondre qu'à un devis qui attend sa réponse (ENVOYE).
     */
    private void verifierEnAttenteDeReponse(Devis devis) {
        if (devis.getStatut() != StatutDevis.ENVOYE) {
            throw new BusinessException(
                    "Ce devis n'attend pas de réponse (statut : " + devis.getStatut() + ")");
        }
    }

    private Devis chercherDevisActuel(Long demandeId) {
        return devisRepository.findFirstByDemandeIdOrderByNumeroVersionDesc(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucun devis n'a encore été envoyé pour la demande " + demandeId));
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