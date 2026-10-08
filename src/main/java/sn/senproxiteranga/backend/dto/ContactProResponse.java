package sn.senproxiteranga.backend.dto;

/**
 * Les numéros d'un professionnel, pour les boutons « WhatsApp » et « Appeler »
 * de son profil. Donnés seulement à une personne connectée (pas au public),
 * pour éviter que des robots récupèrent les numéros de tous les pros.
 */
public record ContactProResponse(
        Long professionnelId,
        String telephone,      // pour le bouton « Appeler »
        String whatsapp        // pour le bouton « WhatsApp » (null s'il n'en a pas)
) {
}
