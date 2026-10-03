package sn.senproxiteranga.backend.domain.enums;

/**
 * Le moyen utilisé par le client pour payer le professionnel.
 * Le paiement se fait directement entre eux : la plateforme l'enregistre et le suit.
 */
public enum ModePaiement {
    ESPECES,
    WAVE,
    ORANGE_MONEY,
    FREE_MONEY
}