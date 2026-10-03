package sn.senproxiteranga.backend.domain.enums;

/**
 * Les états d'un rendez-vous proposé par le professionnel.
 *
 * PROPOSE : le professionnel a proposé une date, le client doit répondre
 * ACCEPTE : le client a accepté la date (la demande passe à PLANIFIEE)
 * REFUSE  : le client a refusé la date, le professionnel doit en proposer une autre
 * REPORTE : la date avait été acceptée mais a été changée (report du client ou du pro)
 */
public enum StatutRendezVous {
    PROPOSE,
    ACCEPTE,
    REFUSE,
    REPORTE
}