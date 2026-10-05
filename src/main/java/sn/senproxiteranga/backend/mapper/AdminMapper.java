package sn.senproxiteranga.backend.mapper;

import org.springframework.stereotype.Component;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.Zone;
import sn.senproxiteranga.backend.dto.ProfessionnelAdminResponse;

import java.util.List;

@Component
public class AdminMapper {

    // Professionnel -> ce que voit l'administrateur
    public ProfessionnelAdminResponse toProfessionnelAdmin(Utilisateur pro) {
        return new ProfessionnelAdminResponse(
                pro.getId(),
                pro.getPrenom() + " " + pro.getNom(),
                pro.getTelephone(),
                pro.getEmail(),
                pro.getMetier(),
                pro.getExperience(),
                nomsDesZones(pro),
                pro.getPhoto() == null ? null : "/api/professionnels/" + pro.getId() + "/photo",
                pro.getStatutVerification(),
                pro.getMotifVerification(),
                pro.getStatutCompte(),
                pro.getCreatedAt()
        );
    }

    // Les noms des zones du pro, par ordre alphabétique
    private List<String> nomsDesZones(Utilisateur pro) {
        return pro.getZones().stream()
                .map(Zone::getNom)
                .sorted()
                .toList();
    }
}