package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.enums.NomRole;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.domain.enums.StatutDemande;
import sn.senproxiteranga.backend.domain.enums.StatutPaiement;
import sn.senproxiteranga.backend.domain.enums.StatutVerification;
import sn.senproxiteranga.backend.dto.ProfessionnelAdminResponse;
import sn.senproxiteranga.backend.dto.StatistiquesResponse;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.AdminMapper;
import sn.senproxiteranga.backend.repository.AvisRepository;
import sn.senproxiteranga.backend.repository.DemandeRepository;
import sn.senproxiteranga.backend.repository.PaiementRepository;
import sn.senproxiteranga.backend.repository.UtilisateurRepository;
import sn.senproxiteranga.backend.service.AdminService;

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
        return adminMapper.toProfessionnelAdmin(pro);
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
    //                          MÉTHODES INTERNES
    // =====================================================================

    // Règle 4 : seulement des professionnels
    private Utilisateur chercherPro(Long professionnelId) {
        return utilisateurRepository.findByIdAndRoleNom(professionnelId, NomRole.PROFESSIONNEL)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Professionnel introuvable : " + professionnelId));
    }
}