package sn.senproxiteranga.backend.mapper;

import org.springframework.stereotype.Component;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.Demande;
import sn.senproxiteranga.backend.domain.RendezVous;
import sn.senproxiteranga.backend.domain.enums.NomRole;
import sn.senproxiteranga.backend.domain.enums.StatutRendezVous;
import sn.senproxiteranga.backend.dto.RendezVousRequest;
import sn.senproxiteranga.backend.dto.RendezVousResponse;

@Component
public class RendezVousMapper {

    /**
     * Nouvelle proposition de date pour une demande.
     * Le statut est PROPOSE : l'autre (client ou professionnel) doit encore répondre.
     */
    public RendezVous toEntity(Demande demande, RendezVousRequest request, NomRole proposePar) {
        RendezVous rendezVous = new RendezVous();
        rendezVous.setDemande(demande);
        rendezVous.setDateHeure(request.dateHeure());
        rendezVous.setProposePar(proposePar);
        rendezVous.setStatut(StatutRendezVous.PROPOSE);
        return rendezVous;
    }

    /**
     * Rendez-vous -> réponse JSON, avec les informations utiles de la demande.
     */
    public RendezVousResponse toResponse(RendezVous rendezVous) {
        Demande demande = rendezVous.getDemande();
        Utilisateur client = demande.getClient();
        Utilisateur pro = demande.getProfessionnel();

        return new RendezVousResponse(
                rendezVous.getId(),
                rendezVous.getDateHeure(),
                rendezVous.getStatut(),
                rendezVous.getProposePar(),
                rendezVous.getMotif(),
                rendezVous.getCreatedAt(),
                rendezVous.getDateDebutTravaux(),
                rendezVous.getDateFinTravaux(),

                demande.getId(),
                demande.getStatut(),
                demande.getService().getTitre(),
                demande.getAdresse(),

                client.getId(),
                client.getPrenom() + " " + client.getNom(),
                client.getTelephone(),
                pro.getId(),
                pro.getPrenom() + " " + pro.getNom()
        );
    }
}
