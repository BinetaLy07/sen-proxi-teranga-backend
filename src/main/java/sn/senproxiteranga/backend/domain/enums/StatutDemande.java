package sn.senproxiteranga.backend.domain.enums;

/**
 * Le cycle de vie d'une demande de service.
 * Le paiement a son propre statut (voir StatutPaiement : DECLARE, CONFIRME, CONTESTE) :
 * pendant que le paiement est déclaré, la demande reste CONFIRMEE.
 */
public enum StatutDemande {

    // ----- Parcours normal -----
    CREEE,          // 1. Le client envoie sa demande
    ACCEPTEE,       // 2. Le professionnel accepte
    DEVIS_ENVOYE,   // 3. Le professionnel envoie un devis (sauté si tarif fixe)
    DEVIS_ACCEPTE,  // 4. Le client accepte le devis
    PLANIFIEE,      // 5. Date et créneau confirmés
    EN_COURS,       // 6. Le professionnel démarre la prestation
    TERMINEE,       // 7. Le professionnel déclare la fin
    CONFIRMEE,      // 8. Le client confirme que c'est bien fait (il peut alors payer)
    CLOTUREE,       // 9. Paiement confirmé : par le pro, ou automatiquement après 48 h sans réponse

    // ----- Statuts de sortie -----
    REFUSEE,        // Le professionnel refuse (motif obligatoire)
    ANNULEE,        // Annulation avant "En cours", devis refusé, ou litige réglé par un dossier annulé
    EXPIREE,        // Le pro n'a pas répondu à temps (48 h, ou 2 h pour une demande urgente)
    EN_LITIGE       // Le pro conteste le paiement : l'administrateur tranche
}