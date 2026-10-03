package sn.senproxiteranga.backend.dto;

import sn.senproxiteranga.backend.domain.enums.StatutDemande;
import sn.senproxiteranga.backend.domain.enums.StatutRendezVous;

import java.time.LocalDateTime;

/**
 * Un rendez-vous renvoyé au client ou au professionnel.
 */
public record RendezVousResponse(
        Long id,
        LocalDateTime dateHeure,
        StatutRendezVous statut,
        String motif,                       // raison du refus ou du report
        LocalDateTime dateProposition,      // quand le pro a proposé cette date
        LocalDateTime dateDebutTravaux,
        LocalDateTime dateFinTravaux,

        // La demande concernée
        Long demandeId,
        StatutDemande statutDemande,
        String serviceTitre,
        String adresse,                     // où se passent les travaux

        // Le client et le professionnel
        Long clientId,
        String clientNom,
        String clientTelephone,
        Long professionnelId,
        String professionnelNom
) {
}