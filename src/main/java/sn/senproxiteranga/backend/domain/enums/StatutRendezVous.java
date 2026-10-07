package sn.senproxiteranga.backend.domain.enums;

/**
 * Les états d'une date de rendez-vous.
 *
 * PROPOSE : une date a été proposée (par le client ou par le professionnel), l'autre doit répondre
 * ACCEPTE : l'autre a accepté la date (la demande passe à PLANIFIEE)
 * REFUSE  : l'autre n'a pas accepté cette date et en a proposé une autre à la place
 * REPORTE : la date avait été acceptée mais a été changée (report du client ou du pro)
 */
public enum StatutRendezVous {
    PROPOSE,
    ACCEPTE,
    REFUSE,
    REPORTE
}
