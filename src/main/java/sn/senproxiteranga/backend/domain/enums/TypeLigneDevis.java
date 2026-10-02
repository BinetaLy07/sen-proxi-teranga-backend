package sn.senproxiteranga.backend.domain.enums;

/**
 * Le type de chaque ligne d'un devis.
 *
 * MAIN_OEUVRE : le travail du professionnel (obligatoire, une seule ligne)
 * MATERIEL    : les pièces ou fournitures (autant de lignes que nécessaire)
 * DEPLACEMENT : les frais de déplacement (facultatif, une seule ligne au maximum)
 */
public enum TypeLigneDevis {
    MAIN_OEUVRE,
    MATERIEL,
    DEPLACEMENT
}