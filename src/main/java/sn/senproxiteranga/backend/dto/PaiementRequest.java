package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sn.senproxiteranga.backend.domain.enums.ModePaiement;

/**
 * Le client déclare avoir payé le professionnel.
 * Le montant n'est PAS envoyé : il est pris dans le devis accepté.
 */
public record PaiementRequest(

        @NotNull(message = "Le moyen de paiement est obligatoire (ESPECES, WAVE, ORANGE_MONEY ou FREE_MONEY)")
        ModePaiement modePaiement,

        // Facultatif : numéro de transaction Wave / Orange Money / Free Money
        @Size(max = 100, message = "La référence ne doit pas dépasser 100 caractères")
        String reference
) {
}