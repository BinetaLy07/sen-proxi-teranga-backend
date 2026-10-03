package sn.senproxiteranga.backend.service;

import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.dto.CreationUtilisateurRequest;
import sn.senproxiteranga.backend.dto.InscriptionClientRequest;
import sn.senproxiteranga.backend.dto.InscriptionProfessionnelRequest;
import sn.senproxiteranga.backend.dto.RegisterRequest;
import sn.senproxiteranga.backend.dto.UtilisateurResponse;

public interface AuthService {

    UtilisateurResponse changerStatutCompte(Long id, StatutCompte statutCompte);

    UtilisateurResponse creerUtilisateur(CreationUtilisateurRequest request);

    UtilisateurResponse register(RegisterRequest request);

    UtilisateurResponse inscrireClient(InscriptionClientRequest request);

    UtilisateurResponse inscrireProfessionnel(InscriptionProfessionnelRequest request);
}
