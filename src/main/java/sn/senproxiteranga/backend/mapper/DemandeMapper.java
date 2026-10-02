package sn.senproxiteranga.backend.mapper;

import org.springframework.stereotype.Component;
import sn.senproxiteranga.backend.domain.Client;
import sn.senproxiteranga.backend.domain.Demande;
import sn.senproxiteranga.backend.domain.Professionnel;
import sn.senproxiteranga.backend.domain.ServiceProfessionnel;
import sn.senproxiteranga.backend.domain.Zone;
import sn.senproxiteranga.backend.dto.DemandeRequest;
import sn.senproxiteranga.backend.dto.DemandeResponse;

@Component
public class DemandeMapper {

    // Formulaire -> nouvelle demande (client, pro, service et zone : ajoutés par le service)
    public Demande toEntity(DemandeRequest request) {
        Demande demande = new Demande();
        updateEntity(demande, request);
        return demande;
    }

    // Copie les champs simples du formulaire (création ou modification)
    public void updateEntity(Demande demande, DemandeRequest request) {
        demande.setDescription(request.description().trim());
        demande.setAdresse(request.adresse().trim());
        demande.setDateSouhaitee(request.dateSouhaitee());
        // Vrai seulement si "urgente" vaut true ; faux s'il vaut false ou s'il est absent
        demande.setUrgente(Boolean.TRUE.equals(request.urgente()));
    }

    // Demande de la base -> réponse envoyée à Angular
    public DemandeResponse toResponse(Demande demande) {
        Client client = demande.getClient();
        Professionnel pro = demande.getProfessionnel();
        ServiceProfessionnel service = demande.getService();
        Zone zone = demande.getZone();

        return new DemandeResponse(
                demande.getId(),
                demande.getDescription(),
                demande.getAdresse(),
                demande.getDateSouhaitee(),
                demande.isUrgente(),
                demande.getStatut(),
                demande.getCreatedAt(),
                demande.getDateExpiration(),
                demande.getMotifRefus(),
                demande.getMotifAnnulation(),

                client.getId(),
                client.getPrenom() + " " + client.getNom(),
                client.getTelephone(),

                pro.getId(),
                pro.getPrenom() + " " + pro.getNom(),

                service.getId(),
                service.getTitre(),
                service.getTypeTarif(),
                service.getMontant(),

                zone != null ? zone.getId() : null,
                zone != null ? zone.getNom() : null
        );
    }
}