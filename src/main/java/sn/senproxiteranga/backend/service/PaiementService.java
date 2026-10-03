package sn.senproxiteranga.backend.service;

import sn.senproxiteranga.backend.dto.PaiementRequest;
import sn.senproxiteranga.backend.dto.PaiementResponse;

import java.util.List;

/**
 * Suivi des paiements entre client et professionnel.
 * L'argent ne passe pas par la plateforme : elle enregistre et suit le paiement.
 */
public interface PaiementService {

    // ===================== Client =====================

    // Le client déclare avoir payé (le pro a 72 h pour confirmer)
    PaiementResponse declarer(Long clientId, Long demandeId, PaiementRequest request);

    // "Mes dépenses" : tous les paiements du client
    List<PaiementResponse> listerParClient(Long clientId);

    // ===================== Professionnel =====================

    // Le pro confirme avoir reçu l'argent -> demande CLOTUREE
    PaiementResponse confirmer(Long professionnelId, Long demandeId);

    // Le pro dit ne pas avoir reçu l'argent -> demande EN_LITIGE
    PaiementResponse contester(Long professionnelId, Long demandeId, String motif);

    // Le pro enregistre lui-même un paiement reçu en espèces -> directement CLOTUREE
    PaiementResponse enregistrerEspeces(Long professionnelId, Long demandeId);

    // "Mes revenus" : tous les paiements reçus par le pro
    List<PaiementResponse> listerParProfessionnel(Long professionnelId);

    // ===================== Consultation =====================

    // Le paiement d'une demande
    PaiementResponse trouverParDemande(Long demandeId);
}
