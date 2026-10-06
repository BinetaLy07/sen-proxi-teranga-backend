package sn.senproxiteranga.backend.service;

import sn.senproxiteranga.backend.dto.ModifierMonCompteRequest;
import sn.senproxiteranga.backend.dto.MonCompteResponse;

// « Mon compte » : l'utilisateur connecté consulte et modifie ses informations
public interface MonCompteService {

    // Mes informations (prénom, nom, email, téléphone, rôle, statut…)
    MonCompteResponse consulter(Long utilisateurId);

    // Modifier prénom, nom, téléphone, adresse et quartier
    MonCompteResponse modifier(Long utilisateurId, ModifierMonCompteRequest request);
}