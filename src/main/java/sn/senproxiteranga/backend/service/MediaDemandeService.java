package sn.senproxiteranga.backend.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import sn.senproxiteranga.backend.dto.MediaResponse;

import java.util.List;

/**
 * Gestion des photos et vidéos jointes à une demande.
 */
public interface MediaDemandeService {

    // Le client ajoute un ou plusieurs fichiers à sa demande
    List<MediaResponse> ajouter(Long clientId, Long demandeId, List<MultipartFile> fichiers);

    // Liste des médias d'une demande (pour le client et le professionnel)
    List<MediaResponse> lister(Long demandeId);

    // Le fichier lui-même, pour l'afficher dans le navigateur
    FichierMedia charger(Long mediaId);

    // Le client retire un média de sa demande
    void supprimer(Long clientId, Long demandeId, Long mediaId);

    /**
     * Ce que renvoie charger() : le fichier + les informations
     * dont le navigateur a besoin pour l'afficher correctement.
     */
    record FichierMedia(Resource ressource, String contentType, String nomOriginal) {
    }
}