package sn.senproxiteranga.backend.mapper;

import org.springframework.stereotype.Component;

import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.Zone;
import sn.senproxiteranga.backend.domain.enums.NomRole;
import sn.senproxiteranga.backend.dto.MonCompteResponse;

@Component
public class MonCompteMapper {

    // Utilisateur -> réponse « Mon compte » (SANS le mot de passe)
    public MonCompteResponse toResponse(Utilisateur utilisateur) {
        boolean professionnel = utilisateur.aRole(NomRole.PROFESSIONNEL);
        Zone zone = utilisateur.getZone();
        return new MonCompteResponse(
                utilisateur.getId(),
                utilisateur.getPrenom(),
                utilisateur.getNom(),
                utilisateur.getEmail(),
                utilisateur.getTelephone(),
                utilisateur.getRole().getNom().name(),
                utilisateur.getStatutCompte(),
                professionnel ? utilisateur.getStatutVerification() : null,
                professionnel ? utilisateur.getMotifVerification() : null,
                // Le pro en a besoin pour pré-remplir son formulaire de profil
                professionnel ? utilisateur.getWhatsapp() : null,
                professionnel ? utilisateur.isAlerteSmsActive() : null,
                utilisateur.getAdresse(),
                zone != null ? zone.getId() : null,
                zone != null ? zone.getNom() : null,
                utilisateur.getCreatedAt());
    }
}
