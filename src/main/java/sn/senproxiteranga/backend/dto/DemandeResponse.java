package sn.senproxiteranga.backend.dto;

import sn.senproxiteranga.backend.domain.enums.StatutDemande;
import sn.senproxiteranga.backend.domain.enums.TypeTarif;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DemandeResponse(
        Long id,
        String description,
        String adresse,
        LocalDate dateSouhaitee,
        boolean urgente,
        StatutDemande statut,
        LocalDateTime dateCreation,
        LocalDateTime dateExpiration,
        String motifRefus,
        String motifAnnulation,

        // Visite avant devis
        boolean visiteDemandee,
        Double fraisVisite,        // null = pas de visite, 0 = gratuite, > 0 = montant

        // Le client
        Long clientId,
        String clientNom,
        String clientTelephone,

        // Le professionnel
        Long professionnelId,
        String professionnelNom,

        // Le service demandé
        Long serviceId,
        String serviceTitre,
        TypeTarif typeTarif,
        Double montant,

        // La zone (facultative)
        Long zoneId,
        String zoneNom
) {
}