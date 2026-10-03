package sn.senproxiteranga.backend.service.implementation;

import sn.senproxiteranga.backend.domain.enums.NomRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.Favori;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.domain.enums.StatutVerification;
import sn.senproxiteranga.backend.dto.FavoriResponse;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.FavoriMapper;
import sn.senproxiteranga.backend.repository.UtilisateurRepository;
import sn.senproxiteranga.backend.repository.FavoriRepository;
import sn.senproxiteranga.backend.service.FavoriService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriServiceImpl implements FavoriService {

    private final FavoriRepository favoriRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final FavoriMapper favoriMapper;

    @Override
    public FavoriResponse ajouter(Long clientId, Long professionnelId) {
        Utilisateur client = chercherClient(clientId);
        Utilisateur pro = utilisateurRepository.findByIdAndRoleNom(professionnelId, NomRole.PROFESSIONNEL)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Professionnel introuvable : " + professionnelId));

        // Règle 1 : seulement un professionnel vérifié et actif
        if (pro.getStatutVerification() != StatutVerification.VALIDE
                || pro.getStatutCompte() == StatutCompte.SUSPENDU) {
            throw new BusinessException("Ce professionnel n'est pas disponible sur la plateforme");
        }

        // Règle 2 : pas de doublon
        if (favoriRepository.existsByClientIdAndProfessionnelId(clientId, professionnelId)) {
            throw new BusinessException("Ce professionnel est déjà dans vos favoris");
        }

        Favori favori = favoriRepository.save(favoriMapper.toEntity(client, pro));
        return favoriMapper.toResponse(favori);
    }

    @Override
    public void retirer(Long clientId, Long professionnelId) {
        Favori favori = favoriRepository.findByClientIdAndProfessionnelId(clientId, professionnelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ce professionnel n'est pas dans vos favoris"));
        favoriRepository.delete(favori);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriResponse> lister(Long clientId) {
        chercherClient(clientId);
        return favoriRepository.findByClientIdOrderByIdDesc(clientId).stream()
                .map(favoriMapper::toResponse)
                .toList();
    }

    private Client chercherClient(Long clientId) {
        return utilisateurRepository.findByIdAndRoleNom(clientId, NomRole.CLIENT)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable : " + clientId));
    }
}
