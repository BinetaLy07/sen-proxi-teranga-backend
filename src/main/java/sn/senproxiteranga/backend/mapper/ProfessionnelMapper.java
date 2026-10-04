package sn.senproxiteranga.backend.mapper;

import org.springframework.stereotype.Component;
import sn.senproxiteranga.backend.domain.Professionnel;
import sn.senproxiteranga.backend.domain.Zone;
import sn.senproxiteranga.backend.dto.ProfessionnelResumeResponse;

import java.util.List;

@Component
public class ProfessionnelMapper {

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
                pro.getNoteMoyenne(),
                nombreAvis,
                pro.getPhoto(),
                nomsDesZones(pro)
        );
    }

    // Les noms des zones du pro, par ordre alphabétique
    private List<String> nomsDesZones(Professionnel pro) {
        return pro.getZones().stream()
                .map(Zone::getNom)
                .sorted()
                .toList();
    }
}