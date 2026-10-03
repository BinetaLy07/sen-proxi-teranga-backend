package sn.senproxiteranga.backend.mapper;

import org.springframework.stereotype.Component;
import sn.senproxiteranga.backend.domain.Client;
import sn.senproxiteranga.backend.domain.Demande;
import sn.senproxiteranga.backend.domain.Paiement;
import sn.senproxiteranga.backend.domain.Professionnel;
import sn.senproxiteranga.backend.domain.enums.ModePaiement;
import sn.senproxiteranga.backend.dto.PaiementResponse;

@Component
public class PaiementMapper {

    /**
     * Crée un paiement pour une demande.
     * Le statut et les dates sont fixés par le service (selon qui enregistre le paiement).
     */
    public Paiement toEntity(Demande demande, Double montant, ModePaiement mode, String reference) {
        Paiement paiement = new Paiement();
        paiement.setDemande(demande);
        paiement.setMontant(montant);
        paiement.setModePaiement(mode);
        paiement.setReference(nettoyer(reference));
        return paiement;
    }

    /**
     * Paiement -> réponse JSON.
     */
    public PaiementResponse toResponse(Paiement paiement) {
        Demande demande = paiement.getDemande();
        Client client = demande.getClient();
        Professionnel pro = demande.getProfessionnel();

        return new PaiementResponse(
                paiement.getId(),
                paiement.getMontant(),
                paiement.getModePaiement(),
                paiement.getReference(),
                paiement.getStatut(),
                paiement.getCreatedAt(),
                paiement.getDateLimiteConfirmation(),
                paiement.getDateReponse(),
                paiement.getMotifContestation(),

                demande.getId(),
                demande.getStatut(),
                demande.getService().getTitre(),

                client.getId(),
                client.getPrenom() + " " + client.getNom(),
                pro.getId(),
                pro.getPrenom() + " " + pro.getNom()
        );
    }

    /**
     * Référence vide ou faite d'espaces -> null (pas de référence).
     * Sinon, on enlève les espaces au début et à la fin.
     */
    private String nettoyer(String reference) {
        if (reference == null || reference.isBlank()) {
            return null;
        }
        return reference.trim();
    }
}