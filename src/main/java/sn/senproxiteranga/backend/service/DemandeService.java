package sn.senproxiteranga.backend.service;

import sn.senproxiteranga.backend.domain.enums.StatutDemande;
import sn.senproxiteranga.backend.dto.AccepterDemandeRequest;
import sn.senproxiteranga.backend.dto.DemandeRequest;
import sn.senproxiteranga.backend.dto.DemandeResponse;
import sn.senproxiteranga.backend.dto.MotifRequest;

import java.util.List;

public interface DemandeService {

    // ----- Côté client -----
    DemandeResponse creer(Long clientId, DemandeRequest request);

    DemandeResponse modifier(Long clientId, Long demandeId, DemandeRequest request);

    DemandeResponse annulerParClient(Long clientId, Long demandeId, MotifRequest request);

    List<DemandeResponse> listerParClient(Long clientId);

    // ----- Côté professionnel -----

    // request peut être null : acceptation simple, sans frais de visite
    DemandeResponse accepter(Long professionnelId, Long demandeId, AccepterDemandeRequest request);

    DemandeResponse refuser(Long professionnelId, Long demandeId, MotifRequest request);

    DemandeResponse annulerParProfessionnel(Long professionnelId, Long demandeId, MotifRequest request);

    List<DemandeResponse> listerParProfessionnel(Long professionnelId, StatutDemande statut);

    // ----- Commun -----
    DemandeResponse trouverParId(Long demandeId);
}