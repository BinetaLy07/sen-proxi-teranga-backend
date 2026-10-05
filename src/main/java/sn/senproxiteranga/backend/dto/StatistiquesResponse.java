package sn.senproxiteranga.backend.dto;

import sn.senproxiteranga.backend.domain.enums.StatutDemande;

import java.util.Map;

// Le tableau de bord de l'administrateur
public record StatistiquesResponse(
        long nombreClients,
        long nombreProfessionnels,
        long professionnelsEnAttente,
        long professionnelsValides,
        long comptesSuspendus,
        long nombreDemandes,
        Map<StatutDemande, Long> demandesParStatut,
        long demandesEnLitige,
        double montantPaiementsConfirmes,
        long nombreAvis
) {
}