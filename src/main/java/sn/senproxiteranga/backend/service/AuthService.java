package sn.senproxiteranga.backend.service;

import sn.senproxiteranga.backend.dto.InscriptionClientRequest;
import sn.senproxiteranga.backend.dto.InscriptionProfessionnelRequest;
import sn.senproxiteranga.backend.dto.UtilisateurResponse;

public interface AuthService {

    UtilisateurResponse inscrireClient(InscriptionClientRequest request);

    UtilisateurResponse inscrireProfessionnel(InscriptionProfessionnelRequest request);
}