package sn.senproxiteranga.backend.dto;

import sn.senproxiteranga.backend.domain.enums.StatutDevis;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Le devis complet renvoyé au client ou au professionnel.
 */
public record DevisResponse(
        Long id,
        int numeroVersion,
        StatutDevis statut,
        Double montantTotal,
        String motifRevision,
        String motifRefus,
        LocalDateTime dateEnvoi,

        // La demande concernée
        Long demandeId,
        String serviceTitre,

        // Le client et le professionnel
        Long clientId,
        String clientNom,
        Long professionnelId,
        String professionnelNom,

        // Les lignes du devis
        List<LigneDevisResponse> lignes
) {}