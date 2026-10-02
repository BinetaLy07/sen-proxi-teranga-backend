package sn.senproxiteranga.backend.mapper;

import org.springframework.stereotype.Component;
import sn.senproxiteranga.backend.domain.Demande;
import sn.senproxiteranga.backend.domain.Devis;
import sn.senproxiteranga.backend.domain.LigneDevis;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.dto.DevisRequest;
import sn.senproxiteranga.backend.dto.DevisResponse;
import sn.senproxiteranga.backend.dto.LigneDevisRequest;
import sn.senproxiteranga.backend.dto.LigneDevisResponse;

@Component
public class DevisMapper {

    // ===================== Request → Entité =====================

    /**
     * Ajoute au devis toutes les lignes envoyées par le professionnel.
     * Chaque appel à ajouterLigne() calcule le montant de la ligne
     * et recalcule le total du devis.
     */
    public void ajouterLignes(Devis devis, DevisRequest request) {
        for (LigneDevisRequest ligneRequest : request.lignes()) {
            devis.ajouterLigne(toLigneEntity(ligneRequest));
        }
    }

    /**
     * Transforme une ligne reçue en entité LigneDevis.
     * Si la quantité n'est pas envoyée, elle vaut 1.
     */
    public LigneDevis toLigneEntity(LigneDevisRequest request) {
        LigneDevis ligne = new LigneDevis();
        ligne.setType(request.type());
        ligne.setLibelle(request.libelle().trim());
        ligne.setQuantite(request.quantite() != null ? request.quantite() : 1);
        ligne.setPrixUnitaire(request.prixUnitaire());
        return ligne;
    }

    // ===================== Entité → Response =====================

    /**
     * Transforme un devis en réponse JSON complète.
     */
    public DevisResponse toResponse(Devis devis) {
        Demande demande = devis.getDemande();
        Utilisateur client = demande.getClient();
        Utilisateur professionnel = demande.getProfessionnel();

        return new DevisResponse(
                devis.getId(),
                devis.getNumeroVersion(),
                devis.getStatut(),
                devis.getMontantTotal(),
                devis.getMotifRevision(),
                devis.getMotifRefus(),
                devis.getCreatedAt(),

                demande.getId(),
                demande.getService().getTitre(),

                client.getId(),
                nomComplet(client),
                professionnel.getId(),
                nomComplet(professionnel),

                devis.getLignes().stream()
                        .map(this::toLigneResponse)
                        .toList()
        );
    }

    /**
     * Transforme une ligne en réponse JSON.
     */
    public LigneDevisResponse toLigneResponse(LigneDevis ligne) {
        return new LigneDevisResponse(
                ligne.getId(),
                ligne.getType(),
                ligne.getLibelle(),
                ligne.getQuantite(),
                ligne.getPrixUnitaire(),
                ligne.getMontant()
        );
    }

    // ===================== Méthode utilitaire =====================

    private String nomComplet(Utilisateur utilisateur) {
        return utilisateur.getPrenom() + " " + utilisateur.getNom();
    }
}