package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.dto.ModifierMonCompteRequest;
import sn.senproxiteranga.backend.dto.MonCompteResponse;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.MonCompteMapper;
import sn.senproxiteranga.backend.mapper.UtilisateurMapper;
import sn.senproxiteranga.backend.repository.UtilisateurRepository;
import sn.senproxiteranga.backend.repository.ZoneRepository;
import sn.senproxiteranga.backend.service.MonCompteService;

@Service
@RequiredArgsConstructor
@Transactional
public class MonCompteServiceImpl implements MonCompteService {

    private final UtilisateurRepository utilisateurRepository;
    private final ZoneRepository zoneRepository;
    private final UtilisateurMapper utilisateurMapper;
    private final MonCompteMapper monCompteMapper;

    @Override
    @Transactional(readOnly = true)
    public MonCompteResponse consulter(Long utilisateurId) {
        return monCompteMapper.toResponse(chercher(utilisateurId));
    }

    @Override
    public MonCompteResponse modifier(Long utilisateurId, ModifierMonCompteRequest request) {
        Utilisateur moi = chercher(utilisateurId);

        // Règle 1 : un numéro ne sert qu'à un seul compte (je peux garder le mien)
        String telephone = utilisateurMapper.normaliserTelephone(request.telephone());
        boolean prisParUnAutre = utilisateurRepository.findByTelephone(telephone)
                .filter(autre -> !autre.getId().equals(utilisateurId))
                .isPresent();
        if (prisParUnAutre) {
            throw new BusinessException("Ce numéro de téléphone est déjà utilisé");
        }

        // Règle 2 : si un quartier est choisi, il doit exister (null = aucun quartier)
        if (request.zoneId() == null) {
            moi.setZone(null);
        } else {
            moi.setZone(zoneRepository.findById(request.zoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone introuvable : " + request.zoneId())));
        }

        moi.setPrenom(request.prenom().trim());
        moi.setNom(request.nom().trim());
        moi.setTelephone(telephone);
        moi.setAdresse(nettoyer(request.adresse()));

        // Pas besoin de save() : l'utilisateur vient de la base, Hibernate enregistre
        // tout seul les changements à la fin de la transaction
        return monCompteMapper.toResponse(moi);
    }

    // ---------- Méthodes internes ----------

    private Utilisateur chercher(Long utilisateurId) {
        return utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + utilisateurId));
    }

    // Une adresse vide devient null
    private String nettoyer(String texte) {
        return (texte == null || texte.isBlank()) ? null : texte.trim();
    }
}