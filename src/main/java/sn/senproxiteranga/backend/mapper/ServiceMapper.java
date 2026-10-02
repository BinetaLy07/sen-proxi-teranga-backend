package sn.senproxiteranga.backend.mapper;

import org.springframework.stereotype.Component;
import sn.senproxiteranga.backend.domain.Categorie;
import sn.senproxiteranga.backend.domain.Professionnel;
import sn.senproxiteranga.backend.domain.ServiceProfessionnel;
import sn.senproxiteranga.backend.dto.ServiceRequest;
import sn.senproxiteranga.backend.dto.ServiceResponse;

@Component
public class ServiceMapper {

    // Formulaire -> nouveau service (catégorie et professionnel : ajoutés par le service)
    public ServiceProfessionnel toEntity(ServiceRequest request) {
        ServiceProfessionnel service = new ServiceProfessionnel();
        updateEntity(service, request);
        return service;
    }

    // Service de la base -> réponse envoyée à Angular
    public ServiceResponse toResponse(ServiceProfessionnel service) {
        Categorie categorie = service.getCategorie();
        Professionnel pro = service.getProfessionnel();
        return new ServiceResponse(
                service.getId(),
                service.getTitre(),
                service.getDescription(),
                service.getTypeTarif(),
                service.getMontant(),
                service.isActif(),
                categorie.getId(),
                categorie.getNom(),
                pro.getId(),
                pro.getPrenom() + " " + pro.getNom()
        );
    }

    // Copie les champs simples du formulaire (création ou modification)
    public void updateEntity(ServiceProfessionnel service, ServiceRequest request) {
        service.setTitre(request.titre().trim());
        service.setDescription(request.description());
        service.setTypeTarif(request.typeTarif());
        service.setMontant(request.montant());
    }
}