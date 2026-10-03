package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.senproxiteranga.backend.domain.Avis;
import sn.senproxiteranga.backend.domain.Demande;
import sn.senproxiteranga.backend.domain.Professionnel;
import sn.senproxiteranga.backend.domain.enums.StatutDemande;
import sn.senproxiteranga.backend.dto.AvisProfessionnelResponse;
import sn.senproxiteranga.backend.dto.AvisRequest;
import sn.senproxiteranga.backend.dto.AvisResponse;
import sn.senproxiteranga.backend.dto.ReponseAvisRequest;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.AvisMapper;
import sn.senproxiteranga.backend.repository.AvisRepository;
import sn.senproxiteranga.backend.repository.DemandeRepository;
import sn.senproxiteranga.backend.repository.ProfessionnelRepository;
import sn.senproxiteranga.backend.service.AvisService;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AvisServiceImpl implements AvisService {

    // Règle 2 : l'avis est possible une fois la fin des travaux confirmée.
    // EN_LITIGE est inclus : le litige porte sur l'argent, pas sur la qualité du travail.
    private static final Set<StatutDemande> STATUTS_AVIS_POSSIBLE = EnumSet.of(
            StatutDemande.CONFIRMEE,
            StatutDemande.CLOTUREE,
            StatutDemande.EN_LITIGE
    );

    private final AvisRepository avisRepository;
    private final DemandeRepository demandeRepository;
    private final ProfessionnelRepository professionnelRepository;
    private final AvisMapper avisMapper;

    // =============== CLIENT ===============

    @Override
    public AvisResponse donner(Long clientId, Long demandeId, AvisRequest request) {
        // Règle 1 : seul le client de la demande donne son avis
        Demande demande = demandeRepository.findByIdAndClientId(demandeId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Demande " + demandeId + " introuvable pour ce client"));

        // Règle 2 : après la confirmation de fin des travaux
        if (!STATUTS_AVIS_POSSIBLE.contains(demande.getStatut())) {
            throw new BusinessException(
                    "Vous pourrez donner votre avis après avoir confirmé la fin des travaux (statut actuel : "
                            + demande.getStatut() + ")");
        }

        // Règle 3 : un seul avis par demande, définitif
        if (avisRepository.existsByDemandeId(demandeId)) {
            throw new BusinessException("Vous avez déjà donné votre avis pour cette demande");
        }

        Avis avis = avisRepository.save(avisMapper.toEntity(demande, request));

        // Règle 4 : la note moyenne du professionnel est recalculée à chaque nouvel avis
        mettreAJourNoteMoyenne(avis.getProfessionnel());

        return avisMapper.toResponse(avis);
    }

    // =============== PROFESSIONNEL ===============

    @Override
    public AvisResponse repondre(Long professionnelId, Long demandeId, ReponseAvisRequest request) {
        // Seul le professionnel concerné peut répondre
        Avis avis = avisRepository.findByDemandeIdAndProfessionnelId(demandeId, professionnelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucun avis vous concernant pour la demande " + demandeId));

        // Règle 5 : une seule réponse par avis
        if (avis.getReponse() != null) {
            throw new BusinessException("Vous avez déjà répondu à cet avis");
        }

        avis.setReponse(request.reponse().trim());
        avis.setDateReponse(LocalDateTime.now());

        return avisMapper.toResponse(avis);
    }

    // =============== CONSULTATION ===============

    @Override
    @Transactional(readOnly = true)
    public AvisResponse trouverParDemande(Long demandeId) {
        if (!demandeRepository.existsById(demandeId)) {
            throw new ResourceNotFoundException("Demande introuvable : " + demandeId);
        }
        Avis avis = avisRepository.findByDemandeId(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucun avis n'a encore été donné pour la demande " + demandeId));
        return avisMapper.toResponse(avis);
    }

    @Override
    @Transactional(readOnly = true)
    public AvisProfessionnelResponse listerParProfessionnel(Long professionnelId) {
        Professionnel pro = professionnelRepository.findById(professionnelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Professionnel introuvable : " + professionnelId));

        List<Avis> avis = avisRepository.findByProfessionnelIdOrderByCreatedAtDesc(professionnelId);
        long nombre = avisRepository.countByProfessionnelId(professionnelId);

        return avisMapper.toProfessionnelResponse(pro, pro.getNoteMoyenne(), nombre, avis);
    }

    // =============== MÉTHODE INTERNE ===============

    /**
     * Recalcule la note moyenne du professionnel, arrondie à 1 chiffre après la virgule.
     * Exemple : (5 + 4 + 5) / 3 = 4.666... -> 4.7
     */
    private void mettreAJourNoteMoyenne(Professionnel pro) {
        Double moyenne = avisRepository.calculerMoyenne(pro.getId());
        double arrondie = (moyenne == null) ? 0.0 : Math.round(moyenne * 10) / 10.0;
        pro.setNoteMoyenne(arrondie);
    }
}