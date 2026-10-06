package sn.senproxiteranga.backend.service;

import sn.senproxiteranga.backend.domain.enums.NomRole;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.dto.CreationUtilisateurRequest;
import sn.senproxiteranga.backend.dto.InscriptionClientRequest;
import sn.senproxiteranga.backend.dto.InscriptionProfessionnelRequest;
import sn.senproxiteranga.backend.dto.RegisterRequest;
import sn.senproxiteranga.backend.dto.UtilisateurResponse;

import java.util.List;

public interface AuthService {

    // Administration : les comptes d'un rôle (ex : tous les clients)
    List<UtilisateurResponse> listerUtilisateurs(NomRole role);

    UtilisateurResponse changerStatutCompte(Long id, StatutCompte statutCompte);

    UtilisateurResponse creerUtilisateur(CreationUtilisateurRequest request);

    UtilisateurResponse register(RegisterRequest request);

    UtilisateurResponse inscrireClient(InscriptionClientRequest request);

    UtilisateurResponse inscrireProfessionnel(InscriptionProfessionnelRequest request);
}
