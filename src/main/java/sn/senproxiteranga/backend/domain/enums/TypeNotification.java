package sn.senproxiteranga.backend.domain.enums;

// Les événements qui déclenchent une notification
public enum TypeNotification {
    NOUVELLE_DEMANDE,      // le pro reçoit une demande
    DEMANDE_ACCEPTEE,      // le client : le pro a accepté
    DEMANDE_REFUSEE,       // le client : le pro a refusé
    DEVIS_RECU,            // le client reçoit un devis
    PAIEMENT_DECLARE,      // le pro : le client dit avoir payé
    PROFIL_VERIFIE,        // le pro : validé, correction demandée ou refusé
    LITIGE_RESOLU,         // client et pro : l'admin a tranché
    NOUVEAU_MESSAGE        // quelqu'un vous a écrit
}