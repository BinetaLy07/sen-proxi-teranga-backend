package sn.senproxiteranga.backend.mapper;

import org.springframework.stereotype.Component;
import sn.senproxiteranga.backend.domain.Professionnel;
import sn.senproxiteranga.backend.domain.Zone;
import sn.senproxiteranga.backend.dto.AvisResponse;
import sn.senproxiteranga.backend.dto.ModifierProfilRequest;
import sn.senproxiteranga.backend.dto.ProfessionnelResumeResponse;
import sn.senproxiteranga.backend.dto.ProfilProfessionnelResponse;
import sn.senproxiteranga.backend.dto.RealisationResponse;
import sn.senproxiteranga.backend.dto.ServiceResponse;

import java.util.List;

@Component
public class ProfessionnelMapper {

    // ===================== Carte de recherche =====================

    /**
     * Professionnel -> carte résumée pour la recherche.
     * Le nombre d'avis est calculé par le service et passé ici.
     */
    public ProfessionnelResumeResponse toResume(Professionnel pro, long nombreAvis) {
        return new ProfessionnelResumeResponse(
                pro.getId(),
                pro.getPrenom() + " " + pro.getNom(),
                pro.getMetier(),
                pro.getDescription(),
                pro.getExperience(),
                pro.getNoteMoyenne(),
                nombreAvis,
                photoUrl(pro),
                nomsDesZones(pro)
        );
    }

    // ===================== Profil complet =====================

    /**
     * Professionnel -> profil public complet.
     * Les listes (services, avis, réalisations) sont déjà transformées par le service.
     */
    public ProfilProfessionnelResponse toProfil(Professionnel pro, long nombreAvis,
                                                List<ServiceResponse> services,
                                                List<AvisResponse> avis,
                                                List<RealisationResponse> realisations) {
        return new ProfilProfessionnelResponse(
                pro.getId(),
                pro.getPrenom(),
                pro.getNom(),
                pro.getMetier(),
                pro.getDescription(),
                pro.getCompetences(),
                pro.getExperience(),
                photoUrl(pro),
                pro.getNoteMoyenne(),
                nombreAvis,
                pro.getCreatedAt(),
                nomsDesZones(pro),
                services,
                avis,
                realisations
        );
    }

    // ===================== Modification du profil =====================

    /**
     * Recopie dans l'entité les informations envoyées par le professionnel.
     */
    public void modifierProfil(Professionnel pro, ModifierProfilRequest request) {
        pro.setDescription(nettoyer(request.description()));
        pro.setCompetences(nettoyer(request.competences()));
        pro.setExperience(request.experience());
        pro.setWhatsapp(normaliserTelephone(request.whatsapp()));
        // Vrai seulement si le pro a coché l'option ; faux sinon
        pro.setAlerteSmsActive(Boolean.TRUE.equals(request.alerteSmsActive()));
    }

    // ===================== Méthodes utilitaires =====================

    /**
     * Adresse de la photo de profil, ou null si le pro n'en a pas.
     */
    private String photoUrl(Professionnel pro) {
        return pro.getPhoto() == null ? null : "/api/professionnels/" + pro.getId() + "/photo";
    }

    // Les noms des zones du pro, par ordre alphabétique
    private List<String> nomsDesZones(Professionnel pro) {
        return pro.getZones().stream()
                .map(Zone::getNom)
                .sorted()
                .toList();
    }

    // Texte vide ou fait d'espaces -> null
    private String nettoyer(String texte) {
        return (texte == null || texte.isBlank()) ? null : texte.trim();
    }

    // "+221781234567" -> "781234567" (même format que les numéros enregistrés à l'inscription)
    private String normaliserTelephone(String numero) {
        String propre = nettoyer(numero);
        if (propre != null && propre.startsWith("+221")) {
            return propre.substring(4);
        }
        return propre;
    }
}