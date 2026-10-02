package sn.senproxiteranga.backend.domain.enums;

/**
 * Les différents états d'un devis.
 *
 * ENVOYE            : le professionnel a envoyé le devis, le client doit répondre
 * REVISION_DEMANDEE : le client demande une modification (2 fois maximum)
 * REMPLACE          : une nouvelle version a remplacé ce devis (on le garde pour l'historique)
 * ACCEPTE           : le client a accepté le devis
 * REFUSE            : le client a refusé le devis (la demande est annulée)
 */
public enum StatutDevis {
    ENVOYE,
    REVISION_DEMANDEE,
    REMPLACE,
    ACCEPTE,
    REFUSE
}