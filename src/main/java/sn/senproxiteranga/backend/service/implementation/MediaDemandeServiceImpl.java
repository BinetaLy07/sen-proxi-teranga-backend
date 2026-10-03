package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sn.senproxiteranga.backend.domain.Demande;
import sn.senproxiteranga.backend.domain.MediaDemande;
import sn.senproxiteranga.backend.domain.enums.StatutDemande;
import sn.senproxiteranga.backend.domain.enums.TypeMedia;
import sn.senproxiteranga.backend.dto.MediaResponse;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.MediaMapper;
import sn.senproxiteranga.backend.repository.DemandeRepository;
import sn.senproxiteranga.backend.repository.MediaDemandeRepository;
import sn.senproxiteranga.backend.service.MediaDemandeService;
import sn.senproxiteranga.backend.service.StockageFichierService;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class MediaDemandeServiceImpl implements MediaDemandeService {

    // Règle 3 : nombre maximum de fichiers par demande
    private static final int MAX_MEDIAS = 5;

    // Règle 4 : formats autorisés -> extension utilisée pour ranger le fichier
    private static final Map<String, String> FORMATS_AUTORISES = Map.of(
            "image/jpeg", "jpg",
            "image/jpg", "jpg",
            "image/png", "png",
            "video/mp4", "mp4"
    );

    // Règle 2 : on peut ajouter ou retirer des médias seulement avant le devis
    private static final Set<StatutDemande> STATUTS_MODIFIABLES = EnumSet.of(
            StatutDemande.CREEE,
            StatutDemande.ACCEPTEE
    );

    private final MediaDemandeRepository mediaRepository;
    private final DemandeRepository demandeRepository;
    private final StockageFichierService stockageService;
    private final MediaMapper mediaMapper;

    // =============== AJOUTER ===============

    @Override
    public List<MediaResponse> ajouter(Long clientId, Long demandeId, List<MultipartFile> fichiers) {
        Demande demande = chercherDemandeDuClient(clientId, demandeId);   // Règle 1
        verifierModifiable(demande);                                       // Règle 2

        if (fichiers == null || fichiers.isEmpty()) {
            throw new BusinessException("Aucun fichier envoyé");
        }

        // Règle 3 : on vérifie le nombre AVANT d'enregistrer quoi que ce soit
        long dejaPresents = mediaRepository.countByDemandeId(demandeId);
        if (dejaPresents + fichiers.size() > MAX_MEDIAS) {
            long placesRestantes = MAX_MEDIAS - dejaPresents;
            throw new BusinessException("5 fichiers maximum par demande : il reste "
                    + placesRestantes + " place(s)");
        }

        // Règles 4 et 5 : on vérifie TOUS les fichiers avant d'en enregistrer un seul
        for (MultipartFile fichier : fichiers) {
            verifierFichier(fichier);
        }

        // Tout est valide : on range chaque fichier et on crée sa fiche
        List<MediaResponse> resultats = new ArrayList<>();
        for (MultipartFile fichier : fichiers) {
            String contentType = contentTypeNormalise(fichier);
            String extension = FORMATS_AUTORISES.get(contentType);
            TypeMedia type = contentType.startsWith("video/") ? TypeMedia.VIDEO : TypeMedia.IMAGE;

            String nomStocke = stockageService.enregistrer(fichier, extension);
            MediaDemande media = mediaMapper.toEntity(demande, fichier, type, contentType, nomStocke);
            resultats.add(mediaMapper.toResponse(mediaRepository.save(media)));
        }
        return resultats;
    }

    // =============== LISTER ===============

    @Override
    @Transactional(readOnly = true)
    public List<MediaResponse> lister(Long demandeId) {
        if (!demandeRepository.existsById(demandeId)) {
            throw new ResourceNotFoundException("Demande introuvable : " + demandeId);
        }
        return mediaRepository.findByDemandeIdOrderByCreatedAtAsc(demandeId).stream()
                .map(mediaMapper::toResponse)
                .toList();
    }

    // =============== CHARGER (afficher) ===============

    @Override
    @Transactional(readOnly = true)
    public FichierMedia charger(Long mediaId) {
        MediaDemande media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Média introuvable : " + mediaId));
        Resource ressource = stockageService.charger(media.getNomStocke());
        return new FichierMedia(ressource, media.getContentType(), media.getNomOriginal());
    }

    // =============== SUPPRIMER ===============

    @Override
    public void supprimer(Long clientId, Long demandeId, Long mediaId) {
        Demande demande = chercherDemandeDuClient(clientId, demandeId);   // Règle 1
        verifierModifiable(demande);                                       // Règle 2

        MediaDemande media = mediaRepository.findByIdAndDemandeId(mediaId, demandeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Média " + mediaId + " introuvable pour la demande " + demandeId));

        // D'abord la fiche dans la base, puis le fichier sur le disque
        mediaRepository.delete(media);
        stockageService.supprimer(media.getNomStocke());
    }

    // =============== MÉTHODES INTERNES ===============

    private Demande chercherDemandeDuClient(Long clientId, Long demandeId) {
        return demandeRepository.findByIdAndClientId(demandeId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Demande " + demandeId + " introuvable pour ce client"));
    }

    private void verifierModifiable(Demande demande) {
        if (!STATUTS_MODIFIABLES.contains(demande.getStatut())) {
            throw new BusinessException(
                    "Les photos et vidéos ne peuvent être ajoutées ou retirées qu'avant l'envoi du devis (statut : "
                            + demande.getStatut() + ")");
        }
    }

    private void verifierFichier(MultipartFile fichier) {
        String nom = fichier.getOriginalFilename();
        if (fichier.isEmpty()) {
            throw new BusinessException("Le fichier " + nom + " est vide");
        }
        if (!FORMATS_AUTORISES.containsKey(contentTypeNormalise(fichier))) {
            throw new BusinessException("Format non autorisé pour " + nom
                    + " : seuls les fichiers JPG, PNG et MP4 sont acceptés");
        }
    }

    // Le type annoncé par le navigateur, en minuscules ("" s'il est absent)
    private String contentTypeNormalise(MultipartFile fichier) {
        String contentType = fichier.getContentType();
        return contentType == null ? "" : contentType.toLowerCase();
    }
}