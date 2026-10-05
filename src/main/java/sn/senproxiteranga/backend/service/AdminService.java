package sn.senproxiteranga.backend.service;

import sn.senproxiteranga.backend.domain.enums.StatutVerification;
import sn.senproxiteranga.backend.dto.DemandeResponse;
import sn.senproxiteranga.backend.dto.ProfessionnelAdminResponse;
import sn.senproxiteranga.backend.dto.ResoudreLitigeRequest;
import sn.senproxiteranga.backend.dto.StatistiquesResponse;

import java.util.List;

public interface AdminService {

    // Les professionnels, filtrés par statut de vérification (null = tous)
    List<ProfessionnelAdminResponse> listerProfessionnels(StatutVerification statut);

    ProfessionnelAdminResponse validerProfessionnel(Long professionnelId);

    ProfessionnelAdminResponse demanderCorrection(Long professionnelId, String motif);

    ProfessionnelAdminResponse refuserProfessionnel(Long professionnelId, String motif);

    // Les demandes EN_LITIGE, les plus anciennes d'abord
    List<DemandeResponse> listerLitiges();

    DemandeResponse resoudreLitige(Long demandeId, ResoudreLitigeRequest request);

    StatistiquesResponse statistiques();
}