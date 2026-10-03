package sn.senproxiteranga.backend.service;

import sn.senproxiteranga.backend.dto.AvisProfessionnelResponse;
import sn.senproxiteranga.backend.dto.AvisRequest;
import sn.senproxiteranga.backend.dto.AvisResponse;
import sn.senproxiteranga.backend.dto.ReponseAvisRequest;

/**
 * Avis des clients sur les professionnels, et réponses des professionnels.
 */
public interface AvisService {

    // Le client donne son avis après avoir confirmé la fin des travaux (définitif)
    AvisResponse donner(Long clientId, Long demandeId, AvisRequest request);

    // Le professionnel répond à l'avis (une seule fois)
    AvisResponse repondre(Long professionnelId, Long demandeId, ReponseAvisRequest request);

    // L'avis d'une demande
    AvisResponse trouverParDemande(Long demandeId);

    // Page du professionnel : note moyenne, nombre d'avis et liste des avis
    AvisProfessionnelResponse listerParProfessionnel(Long professionnelId);
}