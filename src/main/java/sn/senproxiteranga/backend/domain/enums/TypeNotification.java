package sn.senproxiteranga.backend.domain.enums;

// Le type d'une notification : permet au front d'afficher la bonne icône
// et d'ouvrir le bon écran quand on clique dessus
public enum TypeNotification {
    NOUVELLE_DEMANDE,     // au pro : un client lui envoie une demande
    DEMANDE_ACCEPTEE,     // au client : le pro accepte
    DEMANDE_REFUSEE,      // au client : le pro refuse
    DEMANDE_EXPIREE,      // au client : le pro n'a pas répondu à temps (action automatique)
    DEVIS_RECU,           // au client : nouveau devis ou nouvelle version
    PAIEMENT_DECLARE,     // au pro : le client dit avoir payé
    PAIEMENT_CONFIRME,    // aux deux : paiement confirmé automatiquement après 48 h
    PROFIL_VERIFIE,       // au pro : validé, correction demandée ou refusé
    LITIGE_RESOLU,        // aux deux : l'administrateur a tranché
    NOUVEAU_MESSAGE       // au destinataire d'un message
}