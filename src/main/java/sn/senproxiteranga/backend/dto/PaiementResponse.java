package sn.senproxiteranga.backend.dto;

import sn.senproxiteranga.backend.domain.enums.ModePaiement;
import sn.senproxiteranga.backend.domain.enums.StatutDemande;
import sn.senproxiteranga.backend.domain.enums.StatutPaiement;

import java.time.LocalDateTime;

/**
 * Un paiement renvoyé au client ou au professionnel.
 */
public record PaiementResponse(
        Long id,
        Double montant,
        ModePaiement modePaiement,
        String reference,
        StatutPaiement statut,
        LocalDateTime dateDeclaration,
        LocalDateTime dateLimiteConfirmation,   // le pro doit confirmer avant cette date
        LocalDateTime dateReponse,              // quand le pro a confirmé ou contesté
        String motifContestation,

        // La demande concernée
        Long demandeId,
        StatutDemande statutDemande,
        String serviceTitre,

        // Le client et le professionnel
        Long clientId,
        String clientNom,
        Long professionnelId,
        String professionnelNom
) {
}