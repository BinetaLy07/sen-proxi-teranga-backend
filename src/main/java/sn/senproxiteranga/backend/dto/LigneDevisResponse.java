package sn.senproxiteranga.backend.dto;

import sn.senproxiteranga.backend.domain.enums.TypeLigneDevis;

/**
 * Une ligne du devis renvoyée au client ou au professionnel,
 * avec son montant calculé par le serveur.
 */
public record LigneDevisResponse(
        Long id,
        TypeLigneDevis type,
        String libelle,
        int quantite,
        Double prixUnitaire,
        Double montant
) {}