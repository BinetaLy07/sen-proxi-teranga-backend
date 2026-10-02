package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.senproxiteranga.backend.domain.Categorie;
import sn.senproxiteranga.backend.dto.CategorieRequest;
import sn.senproxiteranga.backend.dto.CategorieResponse;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.CategorieMapper;
import sn.senproxiteranga.backend.repository.CategorieRepository;
import sn.senproxiteranga.backend.service.CategorieService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategorieServiceImpl implements CategorieService {

    private final CategorieRepository categorieRepository;
    private final CategorieMapper categorieMapper;

    @Override
    public CategorieResponse creer(CategorieRequest request) {
        if (categorieRepository.existsByNomIgnoreCase(request.nom().trim())) {
            throw new BusinessException("Une catégorie avec ce nom existe déjà");
        }
        Categorie categorie = categorieMapper.toEntity(request);
        Categorie enregistree = categorieRepository.save(categorie);
        return categorieMapper.toResponse(enregistree);
    }

    @Override
    public CategorieResponse modifier(Long id, CategorieRequest request) {
        Categorie categorie = chercher(id);
        String nouveauNom = request.nom().trim();
        boolean nomChange = !categorie.getNom().equalsIgnoreCase(nouveauNom);
        if (nomChange && categorieRepository.existsByNomIgnoreCase(nouveauNom)) {
            throw new BusinessException("Une catégorie avec ce nom existe déjà");
        }
        categorieMapper.updateEntity(categorie, request);
        return categorieMapper.toResponse(categorieRepository.save(categorie));
    }

    @Override
    public CategorieResponse activer(Long id) {
        Categorie categorie = chercher(id);
        categorie.activer();
        return categorieMapper.toResponse(categorieRepository.save(categorie));
    }

    @Override
    public CategorieResponse desactiver(Long id) {
        Categorie categorie = chercher(id);
        categorie.desactiver();
        return categorieMapper.toResponse(categorieRepository.save(categorie));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorieResponse> listerToutes() {
        return categorieRepository.findAll().stream()
                .map(categorieMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorieResponse> listerActives() {
        return categorieRepository.findByActiveTrueOrderByNomAsc().stream()
                .map(categorieMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategorieResponse trouverParId(Long id) {
        return categorieMapper.toResponse(chercher(id));
    }

    // Méthode interne : retrouve une catégorie ou lance "introuvable"
    private Categorie chercher(Long id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + id));
    }
}