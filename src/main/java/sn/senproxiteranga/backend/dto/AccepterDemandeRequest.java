package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.PositiveOrZero;

/**
 * Ce que le professionnel peut envoyer en acceptant une demande.
 * Le corps entier est facultatif : sans corps, l'acceptation marche comme avant.
 *
 * fraisVisite : seulement si le client a demandé une visite.
 *   - absent ou null : visite gratuite
 *   - 0              : visite gratuite
 *   - > 0            : montant de la visite (ex : 1000)
 */
public record AccepterDemandeRequest(

        @PositiveOrZero(message = "Les frais de visite ne peuvent pas être négatifs")
        Double fraisVisite
) {
}