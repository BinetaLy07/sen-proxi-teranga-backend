package sn.senproxiteranga.backend.mapper;

import org.springframework.stereotype.Component;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.Favori;
import sn.senproxiteranga.backend.dto.FavoriResponse;

@Component
public class FavoriMapper {

    // Nouveau favori : ce client ajoute ce professionnel
    public Favori toEntity(Utilisateur client, Utilisateur professionnel) {
        Favori favori = new Favori();
        favori.setClient(client);
        favori.setProfessionnel(professionnel);
        return favori;
    }

    // Favori -> carte du professionnel dans la liste
    public FavoriResponse toResponse(Favori favori) {
        Utilisateur pro = favori.getProfessionnel();
        return new FavoriResponse(
                favori.getId(),
                favori.getCreatedAt(),
                pro.getId(),
                pro.getPrenom() + " " + pro.getNom(),
                pro.getMetier(),
                pro.getNoteMoyenne(),
                pro.getPhoto()
        );
    }
}
