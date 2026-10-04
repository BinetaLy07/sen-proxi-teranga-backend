package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.domain.enums.StatutVerification;
import sn.senproxiteranga.backend.dto.ProfessionnelResumeResponse;
import sn.senproxiteranga.backend.mapper.ProfessionnelMapper;
import sn.senproxiteranga.backend.repository.AvisRepository;
import sn.senproxiteranga.backend.repository.ProfessionnelRepository;
import sn.senproxiteranga.backend.service.ProfessionnelService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfessionnelServiceImpl implements ProfessionnelService {

    private final ProfessionnelRepository professionnelRepository;
    private final AvisRepository avisRepository;
    private final ProfessionnelMapper professionnelMapper;

    @Override
    public List<ProfessionnelResumeResponse> rechercher(String metier, Long zoneId, Long categorieId) {
        return professionnelRepository.rechercher(
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

    // Texte vide ou fait d'espaces -> null (= filtre ignoré)
    private String nettoyer(String texte) {
        return (texte == null || texte.isBlank()) ? null : texte.trim();
    }
}