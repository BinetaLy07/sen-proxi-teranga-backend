package sn.senproxiteranga.backend.domain.enums;

public enum StatutDemande {

    // ----- Parcours normal -----
    CREEE,          // 1. Le client envoie sa demande
    ACCEPTEE,       // 2. Le professionnel accepte
    DEVIS_ENVOYE,   // 3. Le professionnel envoie un devis (sauté si tarif fixe)
    DEVIS_ACCEPTE,  // 4. Le client accepte le devis
    PLANIFIEE,      // 5. Date et créneau confirmés
    EN_COURS,       // 6. Le professionnel démarre la prestation
    TERMINEE,       // 7. Le professionnel déclare la fin
    CONFIRMEE,      // 8. Le client confirme que c'est bien fait
    PAYEE,          // 9. Paiement déclaré par le client
    CLOTUREE,       // 10. Paiement confirmé par le professionnel

    // ----- Statuts de sortie -----
    REFUSEE,        // Le professionnel refuse (motif obligatoire)
    ANNULEE,        // Annulation avant "En cours", ou devis refusé
    EXPIREE,        // Pas de réponse dans le délai (ex : 48 h)
    EN_LITIGE       // Réclamation, ou paiement non confirmé à temps (72 h)
}