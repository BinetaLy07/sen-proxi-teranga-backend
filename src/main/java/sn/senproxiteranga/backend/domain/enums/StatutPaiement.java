package sn.senproxiteranga.backend.domain.enums;

/**
 * Les états d'un paiement.
 *
 * DECLARE  : le client dit avoir payé, le professionnel doit confirmer (72 h maximum)
 * CONFIRME : le professionnel a confirmé la réception (la demande est clôturée)
 * CONTESTE : le professionnel dit ne pas avoir reçu l'argent (la demande passe en litige)
 */
public enum StatutPaiement {
    DECLARE,
    CONFIRME,
    CONTESTE
}