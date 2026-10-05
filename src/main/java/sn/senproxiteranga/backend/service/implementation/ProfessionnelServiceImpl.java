package sn.senproxiteranga.backend.service.implementation;

import sn.senproxiteranga.backend.domain.enums.NomRole;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.Realisation;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.domain.enums.StatutVerification;
import sn.senproxiteranga.backend.dto.AvisResponse;
import sn.senproxiteranga.backend.dto.ModifierProfilRequest;
import sn.senproxiteranga.backend.dto.ProfessionnelResumeResponse;
import sn.senproxiteranga.backend.dto.ProfilProfessionnelResponse;
import sn.senproxiteranga.backend.dto.RealisationResponse;
import sn.senproxiteranga.backend.dto.ServiceResponse;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.AvisMapper;
import sn.senproxiteranga.backend.mapper.ProfessionnelMapper;
import sn.senproxiteranga.backend.mapper.RealisationMapper;
import sn.senproxiteranga.backend.mapper.ServiceMapper;
import sn.senproxiteranga.backend.repository.AvisRepository;
import sn.senproxiteranga.backend.repository.UtilisateurRepository;
import sn.senproxiteranga.backend.repository.RealisationRepository;
import sn.senproxiteranga.backend.repository.ServiceProfessionnelRepository;
import sn.senproxiteranga.backend.service.ProfessionnelService;
import sn.senproxiteranga.backend.service.StockageFichierService;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfessionnelServiceImpl implements ProfessionnelService {

    // Nombre maximum de réalisations dans la galerie d'un pro
    private static final int MAX_REALISATIONS = 10;

    // Formats d'image acceptés -> extension utilisée sur le disque
    private static final Map<String, String> FORMATS_IMAGES = Map.of(
            "image/jpeg", "jpg",
            "image/jpg", "jpg",
            "image/png", "png"
    );

    private final UtilisateurRepository utilisateurRepository;
    private final ServiceProfessionnelRepository serviceRepository;
    private final AvisRepository avisRepository;
    private final RealisationRepository realisationRepository;
    private final StockageFichierService stockageService;
    private final ProfessionnelMapper professionnelMapper;
    private final ServiceMapper serviceMapper;
    private final AvisMapper avisMapper;
    private final RealisationMapper realisationMapper;

    // =====================================================================
    //                       VISITEURS ET CLIENTS
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public List<ProfessionnelResumeResponse> rechercher(String metier, Long zoneId, Long categorieId) {
        return utilisateurRepository.rechercher(
                        nettoyer(metier),
                        zoneId,
                        categorieId,
                        StatutVerification.VALIDE,     // seulement les pros vérifiés
                        StatutCompte.ACTIF)            // et non suspendus
                .stream()
                .map(pro -> professionnelMapper.toResume(
                        pro, avisRepository.countByProfessionnelId(pro.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProfilProfessionnelResponse profilPublic(Long professionnelId) {
        Utilisateur pro = chercherPro(professionnelId);

        // Règle 1 : un pro non validé ou suspendu n'est pas visible du public
        if (pro.getStatutVerification() != StatutVerification.VALIDE
                || pro.getStatutCompte() == StatutCompte.SUSPENDU) {
            throw new ResourceNotFoundException("Professionnel introuvable : " + professionnelId);
        }
        return construireProfil(pro);
    }

    // =====================================================================
    //                     ESPACE DU PROFESSIONNEL
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public ProfilProfessionnelResponse monProfil(Long professionnelId) {
        return construireProfil(chercherPro(professionnelId));
    }

    @Override
    public ProfilProfessionnelResponse modifierProfil(Long professionnelId, ModifierProfilRequest request) {
        Utilisateur pro = chercherPro(professionnelId);
        professionnelMapper.modifierProfil(pro, request);
        renvoyerEnVerificationSiCorrection(pro);                           // Règle 5
        return construireProfil(pro);
    }

    @Override
    public ProfilProfessionnelResponse changerPhoto(Long professionnelId, MultipartFile photo) {
        Utilisateur pro = chercherPro(professionnelId);
        String extension = verifierImage(photo);                           // Règle 2

        String anciennePhoto = pro.getPhoto();
        pro.setPhoto(stockageService.enregistrer(photo, extension));

        // L'ancienne photo ne sert plus : on la supprime du disque
        if (anciennePhoto != null) {
            stockageService.supprimer(anciennePhoto);
        }
        renvoyerEnVerificationSiCorrection(pro);                           // Règle 5
        return construireProfil(pro);
    }

    @Override
    public RealisationResponse ajouterRealisation(Long professionnelId, MultipartFile photo,
                                                  String titre, String description) {
        Utilisateur pro = chercherPro(professionnelId);

        // Règle 3 : un titre obligatoire et des tailles raisonnables
        if (titre == null || titre.isBlank()) {
            throw new BusinessException("Le titre de la réalisation est obligatoire");
        }
        if (titre.trim().length() > 150) {
            throw new BusinessException("Le titre ne doit pas dépasser 150 caractères");
        }
        if (description != null && description.trim().length() > 500) {
            throw new BusinessException("La description ne doit pas dépasser 500 caractères");
        }

        // Règle 4 : 10 réalisations maximum
        if (realisationRepository.countByProfessionnelId(professionnelId) >= MAX_REALISATIONS) {
            throw new BusinessException("Vous avez déjà " + MAX_REALISATIONS
                    + " réalisations : supprimez-en une pour en ajouter une nouvelle");
        }

        String extension = verifierImage(photo);                           // Règle 2
        String nomStocke = stockageService.enregistrer(photo, extension);
        Realisation realisation = realisationMapper.toEntity(
                pro, photo, titre, description, contentTypeNormalise(photo), nomStocke);

        renvoyerEnVerificationSiCorrection(pro);                           // Règle 5
        return realisationMapper.toResponse(realisationRepository.save(realisation));
    }

    @Override
    public void supprimerRealisation(Long professionnelId, Long realisationId) {
        Realisation realisation = realisationRepository.findByIdAndProfessionnelId(realisationId, professionnelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Réalisation " + realisationId + " introuvable pour ce professionnel"));

        // D'abord la fiche dans la base, puis la photo sur le disque
        realisationRepository.delete(realisation);
        stockageService.supprimer(realisation.getNomStocke());
    }

    // =====================================================================
    //                       AFFICHAGE DES IMAGES
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public FichierImage chargerPhoto(Long professionnelId) {
        Utilisateur pro = chercherPro(professionnelId);
        verifierProfilVisible(pro);
        if (pro.getPhoto() == null) {
            throw new ResourceNotFoundException("Ce professionnel n'a pas de photo de profil");
        }
        String contentType = pro.getPhoto().endsWith(".png") ? "image/png" : "image/jpeg";
        return new FichierImage(stockageService.charger(pro.getPhoto()), contentType);
    }

    @Override
    @Transactional(readOnly = true)
    public FichierImage chargerRealisation(Long realisationId) {
        Realisation realisation = realisationRepository.findById(realisationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réalisation introuvable : " + realisationId));
        verifierProfilVisible(realisation.getProfessionnel());
        Resource ressource = stockageService.charger(realisation.getNomStocke());
        return new FichierImage(ressource, realisation.getContentType());
    }

    // =====================================================================
    //                        MÉTHODES INTERNES
    // =====================================================================

    /**
     * Rassemble tout le profil : services actifs, avis, réalisations.
     * On réutilise les mappers des autres modules.
     */
    private ProfilProfessionnelResponse construireProfil(Utilisateur pro) {
        Long id = pro.getId();

        List<ServiceResponse> services = serviceRepository
                .findByProfessionnelIdAndActifTrueOrderByTitreAsc(id).stream()
                .map(serviceMapper::toResponse)
                .toList();

        List<AvisResponse> avis = avisRepository
                .findByProfessionnelIdOrderByCreatedAtDesc(id).stream()
                .map(avisMapper::toResponse)
                .toList();

        List<RealisationResponse> realisations = realisationRepository
                .findByProfessionnelIdOrderByIdDesc(id).stream()
                .map(realisationMapper::toResponse)
                .toList();

        return professionnelMapper.toProfil(
                pro, avisRepository.countByProfessionnelId(id), services, avis, realisations);
    }

    /**
     * Règle 2 : seulement des images JPG ou PNG, non vides.
     * Renvoie l'extension à utiliser sur le disque.
     */
    private String verifierImage(MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            throw new BusinessException("La photo est vide");
        }
        String extension = FORMATS_IMAGES.get(contentTypeNormalise(photo));
        if (extension == null) {
            throw new BusinessException("Format non autorisé : seules les images JPG et PNG sont acceptées");
        }
        return extension;
    }

    private String contentTypeNormalise(MultipartFile fichier) {
        String contentType = fichier.getContentType();
        return contentType == null ? "" : contentType.toLowerCase();
    }

    /**
     * Règle 5 : quand l'administrateur a demandé une correction, le pro repasse
     * automatiquement EN_ATTENTE dès qu'il modifie son profil (texte, photo ou réalisation).
     * Le motif est gardé : l'admin voit ce qu'il avait demandé de corriger.
     * Un pro déjà VALIDE qui modifie son profil reste VALIDE.
     */
    private void renvoyerEnVerificationSiCorrection(Utilisateur pro) {
        if (pro.getStatutVerification() == StatutVerification.CORRECTION_DEMANDEE) {
            pro.setStatutVerification(StatutVerification.EN_ATTENTE);
        }
    }

    private Utilisateur chercherPro(Long professionnelId) {
        return utilisateurRepository.findByIdAndRoleNom(professionnelId, NomRole.PROFESSIONNEL)
                .orElseThrow(() -> new ResourceNotFoundException("Professionnel introuvable : " + professionnelId));
    }

    private void verifierProfilVisible(Utilisateur pro) {
        if (!pro.aRole(NomRole.PROFESSIONNEL)
                || pro.getStatutVerification() != StatutVerification.VALIDE
                || pro.getStatutCompte() != StatutCompte.ACTIF) {
            throw new ResourceNotFoundException("Professionnel introuvable : " + pro.getId());
        }
    }

    private String nettoyer(String texte) {
        return (texte == null || texte.isBlank()) ? null : texte.trim();
    }
}