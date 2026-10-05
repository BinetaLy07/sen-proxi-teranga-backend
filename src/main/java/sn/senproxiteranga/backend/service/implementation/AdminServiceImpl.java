package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.senproxiteranga.backend.domain.Demande;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.enums.DecisionLitige;
import sn.senproxiteranga.backend.domain.enums.NomRole;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.domain.enums.StatutDemande;
import sn.senproxiteranga.backend.domain.enums.StatutPaiement;
import sn.senproxiteranga.backend.domain.enums.StatutVerification;
import sn.senproxiteranga.backend.domain.enums.TypeNotification;
import sn.senproxiteranga.backend.dto.DemandeResponse;
import sn.senproxiteranga.backend.dto.ProfessionnelAdminResponse;
import sn.senproxiteranga.backend.dto.ResoudreLitigeRequest;
import sn.senproxiteranga.backend.dto.StatistiquesResponse;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.AdminMapper;
import sn.senproxiteranga.backend.mapper.DemandeMapper;
import sn.senproxiteranga.backend.repository.AvisRepository;
import sn.senproxiteranga.backend.repository.DemandeRepository;
import sn.senproxiteranga.backend.repository.PaiementRepository;
import sn.senproxiteranga.backend.repository.UtilisateurRepository;
import sn.senproxiteranga.backend.service.AdminService;
import sn.senproxiteranga.backend.service.NotificationService;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UtilisateurRepository utilisateurRepository;
    private final DemandeRepository demandeRepository;
    private final PaiementRepository paiementRepository;
    private final AvisRepository avisRepository;
    private final AdminMapper adminMapper;
    private final DemandeMapper demandeMapper;
    private final NotificationService notificationService;

    // =====================================================================
    //                    VÉRIFICATION DES PROFESSIONNELS
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public List<ProfessionnelAdminResponse> listerProfessionnels(StatutVerification statut) {
        List<Utilisateur> pros = (statut == null)
                ? utilisateurRepository.findByRoleNomOrderByCreatedAtAsc(NomRole.PROFESSIONNEL)
                : utilisateurRepository.findByRoleNomAndStatutVerificationOrderByCreatedAtAsc(
                NomRole.PROFESSIONNEL, statut);
        return pros.stream()
                .map(adminMapper::toProfessionnelAdmin)
                .toList();
    }

    @Override
    public ProfessionnelAdminResponse validerProfessionnel(Long professionnelId) {
        Utilisateur pro = chercherPro(professionnelId);                       // Règle 4
        if (pro.getStatutVerification() == StatutVerification.VALIDE) {       // Règle 1
            throw new BusinessException("Ce professionnel est déjà validé");
        }
        pro.setStatutVerification(StatutVerification.VALIDE);
        pro.setMotifVerification(null);                                       // Règle 5
        prevenirPro(pro, "Profil validé",
                "Bonne nouvelle : votre profil a été vérifié par l'administrateur. "
                        + "Les clients peuvent maintenant vous trouver et vous contacter.");
        return adminMapper.toProfessionnelAdmin(pro);
    }

    @Override
    public ProfessionnelAdminResponse demanderCorrection(Long professionnelId, String motif) {
        Utilisateur pro = chercherPro(professionnelId);                       // Règle 4
        if (pro.getStatutVerification() != StatutVerification.EN_ATTENTE) {   // Règle 2
            throw new BusinessException(
                    "On ne peut demander une correction qu'à un professionnel en attente de vérification (statut : "
                            + pro.getStatutVerification() + ")");
        }
        pro.setStatutVerification(StatutVerification.CORRECTION_DEMANDEE);
        pro.setMotifVerification(motif.trim());
        prevenirPro(pro, "Correction demandée",
                "L'administrateur vous demande de corriger votre profil. "
                        + "Ouvrez votre profil pour voir ce qu'il faut modifier.");
        return adminMapper.toProfessionnelAdmin(pro);
    }

    @Override
    public ProfessionnelAdminResponse refuserProfessionnel(Long professionnelId, String motif) {
        Utilisateur pro = chercherPro(professionnelId);                       // Règle 4
        if (pro.getStatutVerification() == StatutVerification.VALIDE) {       // Règle 3
            throw new BusinessException(
                    "Ce professionnel est déjà validé : pour l'écarter, suspendez son compte");
        }
        if (pro.getStatutVerification() == StatutVerification.REFUSE) {
            throw new BusinessException("Ce professionnel est déjà refusé");
        }
        pro.setStatutVerification(StatutVerification.REFUSE);
        pro.setMotifVerification(motif.trim());
        prevenirPro(pro, "Profil refusé",
                "Votre profil n'a pas été accepté par l'administrateur. "
                        + "Ouvrez votre profil pour voir le motif.");
        return adminMapper.toProfessionnelAdmin(pro);
    }

    // =====================================================================
    //                               LITIGES
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public List<DemandeResponse> listerLitiges() {
        // Les plus anciens d'abord : ceux qui attendent depuis le plus longtemps
        return demandeRepository.findByStatutOrderByUpdatedAtAsc(StatutDemande.EN_LITIGE).stream()
                .map(demandeMapper::toResponse)
                .toList();
    }

    @Override
    public DemandeResponse resoudreLitige(Long demandeId, ResoudreLitigeRequest request) {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : " + demandeId));

        // Règle 6 : on ne règle qu'une demande EN_LITIGE
        if (demande.getStatut() != StatutDemande.EN_LITIGE) {
            throw new BusinessException(
                    "Cette demande n'est pas en litige (statut : " + demande.getStatut() + ")");
        }

        // Règle 7 : l'explication de l'admin est gardée, client et pro la voient
        demande.setResolutionLitige(request.explication().trim());

        if (request.decision() == DecisionLitige.PAIEMENT_RECU) {
            // Règle 8 : la preuve est acceptée => paiement confirmé, dossier clôturé
            paiementRepository.findByDemandeId(demandeId).ifPresent(paiement -> {
                paiement.setStatut(StatutPaiement.CONFIRME);
                paiement.setDateReponse(LocalDateTime.now());
            });
            demande.setStatut(StatutDemande.CLOTUREE);
        } else {
            // Règle 9 : le dossier ne peut pas être réglé dans l'application => annulé
            demande.setStatut(StatutDemande.ANNULEE);
            demande.setMotifAnnulation("Litige réglé par l'administrateur : dossier annulé");
        }

        prevenirLitigeResolu(demande, request.decision());
        return demandeMapper.toResponse(demande);
    }

    // =====================================================================
    //                            STATISTIQUES
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public StatistiquesResponse statistiques() {
        // Le nombre de demandes pour chaque statut, dans l'ordre de l'enum
        Map<StatutDemande, Long> demandesParStatut = new LinkedHashMap<>();
        for (StatutDemande statut : StatutDemande.values()) {
            demandesParStatut.put(statut, demandeRepository.countByStatut(statut));
        }

        return new StatistiquesResponse(
                utilisateurRepository.countByRoleNom(NomRole.CLIENT),
                utilisateurRepository.countByRoleNom(NomRole.PROFESSIONNEL),
                utilisateurRepository.countByRoleNomAndStatutVerification(
                        NomRole.PROFESSIONNEL, StatutVerification.EN_ATTENTE),
                utilisateurRepository.countByRoleNomAndStatutVerification(
                        NomRole.PROFESSIONNEL, StatutVerification.VALIDE),
                utilisateurRepository.countByStatutCompte(StatutCompte.SUSPENDU),
                demandeRepository.count(),
                demandesParStatut,
                demandesParStatut.get(StatutDemande.EN_LITIGE),
                paiementRepository.sommeDesMontants(StatutPaiement.CONFIRME),
                avisRepository.count()
        );
    }

    // =====================================================================
    //                            NOTIFICATIONS
    // =====================================================================

    // Résultat de la vérification du profil : on prévient le pro
    private void prevenirPro(Utilisateur pro, String titre, String message) {
        notificationService.notifier(pro, TypeNotification.PROFIL_VERIFIE, titre, message, null);
    }

    // Litige réglé : le client ET le pro sont prévenus de la décision
    private void prevenirLitigeResolu(Demande demande, DecisionLitige decision) {
        String resultat = (decision == DecisionLitige.PAIEMENT_RECU)
                ? "le paiement est reconnu et le dossier est clôturé."
                : "le dossier est annulé.";
        String message = "L'administrateur a réglé le litige sur la demande : "
                + demande.getService().getTitre() + ". Décision : " + resultat
                + " Ouvrez la demande pour lire son explication.";

        notificationService.notifier(demande.getClient(),
                TypeNotification.LITIGE_RESOLU, "Litige réglé", message, demande.getId());
        notificationService.notifier(demande.getProfessionnel(),
                TypeNotification.LITIGE_RESOLU, "Litige réglé", message, demande.getId());
    }

    // =====================================================================
    //                          MÉTHODES INTERNES
    // =====================================================================

    // Règle 4 : seulement des professionnels
    private Utilisateur chercherPro(Long professionnelId) {
        return utilisateurRepository.findByIdAndRoleNom(professionnelId, NomRole.PROFESSIONNEL)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Professionnel introuvable : " + professionnelId));
    }
}