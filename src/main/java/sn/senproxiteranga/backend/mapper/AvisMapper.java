package sn.senproxiteranga.backend.mapper;

import org.springframework.stereotype.Component;
import sn.senproxiteranga.backend.domain.Avis;
import sn.senproxiteranga.backend.domain.Demande;
import sn.senproxiteranga.backend.domain.Professionnel;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.dto.AvisProfessionnelResponse;
import sn.senproxiteranga.backend.dto.AvisRequest;
import sn.senproxiteranga.backend.dto.AvisResponse;

import java.util.List;

@Component
public class AvisMapper {

    /**
     * Nouvel avis du client sur la demande.
     * Le professionnel est pris dans la demande (le client ne le choisit pas).
     */
    public Avis toEntity(Demande demande, AvisRequest request) {
        Avis avis = new Avis();
        avis.setDemande(demande);
        avis.setProfessionnel(demande.getProfessionnel());
        avis.setNote(request.note());
        avis.setCommentaire(nettoyer(request.commentaire()));
        return avis;
    }

    /**
     * Avis -> réponse JSON (nom du client abrégé, car les avis sont publics).
     */
    public AvisResponse toResponse(Avis avis) {
        Demande demande = avis.getDemande();
        Professionnel pro = avis.getProfessionnel();

        return new AvisResponse(
                avis.getId(),
                avis.getNote(),
                avis.getCommentaire(),
                avis.getCreatedAt(),
                avis.getReponse(),
                avis.getDateReponse(),

                demande.getId(),
                demande.getService().getTitre(),

                nomAbrege(demande.getClient()),
                pro.getId(),
                pro.getPrenom() + " " + pro.getNom()
        );
    }

    /**
     * Résumé pour la page du professionnel : moyenne, nombre d'avis et liste.
     */
    public AvisProfessionnelResponse toProfessionnelResponse(Professionnel pro, double noteMoyenne,
                                                             long nombreAvis, List<Avis> avis) {
        return new AvisProfessionnelResponse(
                pro.getId(),
                pro.getPrenom() + " " + pro.getNom(),
                noteMoyenne,
                nombreAvis,
                avis.stream().map(this::toResponse).toList()
        );
    }

    /**
     * "Awa Diop" -> "Awa D."
     * Si le nom est vide, on affiche seulement le prénom.
     */
    private String nomAbrege(Utilisateur utilisateur) {
        String nom = utilisateur.getNom();
        if (nom == null || nom.isBlank()) {
            return utilisateur.getPrenom();
        }
        return utilisateur.getPrenom() + " " + nom.trim().charAt(0) + ".";
    }

    /**
     * Commentaire vide ou fait d'espaces -> null (pas de commentaire).
     */
    private String nettoyer(String texte) {
        if (texte == null || texte.isBlank()) {
            return null;
        }
        return texte.trim();
    }
}