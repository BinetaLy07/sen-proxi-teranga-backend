package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.senproxiteranga.backend.domain.Categorie;
import sn.senproxiteranga.backend.domain.Professionnel;
import sn.senproxiteranga.backend.domain.ServiceProfessionnel;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.domain.enums.TypeTarif;
import sn.senproxiteranga.backend.dto.ServiceRequest;
import sn.senproxiteranga.backend.dto.ServiceResponse;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.ServiceMapper;
import sn.senproxiteranga.backend.repository.CategorieRepository;
import sn.senproxiteranga.backend.repository.ProfessionnelRepository;
import sn.senproxiteranga.backend.repository.ServiceProfessionnelRepository;
import sn.senproxiteranga.backend.service.ServiceProfessionnelService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ServiceProfessionnelServiceImpl implements ServiceProfessionnelService {

    private final ServiceProfessionnelRepository serviceRepository;
    private final ProfessionnelRepository professionnelRepository;
    private final CategorieRepository categorieRepository;
    private final ServiceMapper serviceMapper;

    @Override
    public ServiceResponse creer(Long professionnelId, ServiceRequest request) {
        Professionnel pro = chercherProfessionnelActif(professionnelId);
        verifierTarif(request);
        Categorie categorie = chercherCategorieActive(request.categorieId());

        ServiceProfessionnel service = serviceMapper.toEntity(request);
        service.setProfessionnel(pro);
        service.setCategorie(categorie);
        return serviceMapper.toResponse(serviceRepository.save(service));
    }

    @Override
    public ServiceResponse modifier(Long professionnelId, Long serviceId, ServiceRequest request) {
        chercherProfessionnelActif(professionnelId);
        ServiceProfessionnel service = chercherServiceDuPro(professionnelId, serviceId);
        verifierTarif(request);

        // On ne vérifie la catégorie que si elle change
        if (!service.getCategorie().getId().equals(request.categorieId())) {
            service.setCategorie(chercherCategorieActive(request.categorieId()));
        }
        serviceMapper.updateEntity(service, request);
        return serviceMapper.toResponse(serviceRepository.save(service));
    }

    @Override
    public ServiceResponse activer(Long professionnelId, Long serviceId) {
        ServiceProfessionnel service = chercherServiceDuPro(professionnelId, serviceId);
        service.activer();
        return serviceMapper.toResponse(serviceRepository.save(service));
    }

    @Override
    public ServiceResponse desactiver(Long professionnelId, Long serviceId) {
        ServiceProfessionnel service = chercherServiceDuPro(professionnelId, serviceId);
        service.desactiver();
        return serviceMapper.toResponse(serviceRepository.save(service));
    }

    @Override
    public void supprimer(Long professionnelId, Long serviceId) {
        ServiceProfessionnel service = chercherServiceDuPro(professionnelId, serviceId);
        serviceRepository.delete(service);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> listerParProfessionnel(Long professionnelId, boolean actifsSeulement) {
        if (!professionnelRepository.existsById(professionnelId)) {
            throw new ResourceNotFoundException("Professionnel introuvable : " + professionnelId);
        }
        List<ServiceProfessionnel> services = actifsSeulement
                ? serviceRepository.findByProfessionnelIdAndActifTrueOrderByTitreAsc(professionnelId)
                : serviceRepository.findByProfessionnelIdOrderByTitreAsc(professionnelId);
        return services.stream()
                .map(serviceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse trouverParId(Long serviceId) {
        ServiceProfessionnel service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service introuvable : " + serviceId));
        return serviceMapper.toResponse(service);
    }

    // ---------- Méthodes internes ----------

    // Règle 1 : le professionnel doit exister et ne pas être suspendu
    private Professionnel chercherProfessionnelActif(Long professionnelId) {
        Professionnel pro = professionnelRepository.findById(professionnelId)
                .orElseThrow(() -> new ResourceNotFoundException("Professionnel introuvable : " + professionnelId));
        if (pro.getStatutCompte() == StatutCompte.SUSPENDU) {
            throw new BusinessException("Votre compte est suspendu : vous ne pouvez pas gérer vos services");
        }
        return pro;
    }

    // Règle 2 : la catégorie doit exister et être active
    private Categorie chercherCategorieActive(Long categorieId) {
        Categorie categorie = categorieRepository.findById(categorieId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + categorieId));
        if (!categorie.isActive()) {
            throw new BusinessException("Cette catégorie n'est pas disponible");
        }
        return categorie;
    }

    // Règle 3 : le montant dépend du type de tarif
    private void verifierTarif(ServiceRequest request) {
        if (request.typeTarif() == TypeTarif.SUR_DEVIS) {
            if (request.montant() != null) {
                throw new BusinessException("Un service sur devis ne doit pas avoir de montant");
            }
        } else if (request.montant() == null) {
            throw new BusinessException("Le montant est obligatoire pour un tarif fixe ou « à partir de »");
        }
    }

    // Règle 4 : un professionnel ne peut toucher qu'à SES propres services
    private ServiceProfessionnel chercherServiceDuPro(Long professionnelId, Long serviceId) {
        return serviceRepository.findByIdAndProfessionnelId(serviceId, professionnelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Service " + serviceId + " introuvable pour ce professionnel"));
    }
}