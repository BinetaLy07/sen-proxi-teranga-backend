package sn.senproxiteranga.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Le devis envoyé par le professionnel : uniquement la liste de ses lignes.
 * Le total est calculé par le serveur.
 */
public record DevisRequest(

        @NotEmpty(message = "Le devis doit contenir au moins une ligne")
        @Valid
        List<LigneDevisRequest> lignes
) {}