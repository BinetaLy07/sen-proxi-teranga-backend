package sn.senproxiteranga.backend.domain.enums;

// Les décisions possibles de l'administrateur pour régler un litige
public enum DecisionLitige {

    // La preuve montre que le client a bien payé :
    // le paiement est confirmé et le dossier est clôturé
    PAIEMENT_RECU,

    // Le client n'a pas payé, ou le dossier ne peut pas être réglé dans l'application :
    // la demande est annulée (la suite se règle hors de la plateforme)
    DOSSIER_ANNULE
}