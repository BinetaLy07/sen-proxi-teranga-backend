package sn.senproxiteranga.backend.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Rangement des fichiers envoyés (photos, vidéos) sur le disque du serveur.
 * Cette classe ne connaît aucune règle métier : elle range, lit et supprime des fichiers.
 */
public interface StockageFichierService {

    // Range le fichier sous un nom unique et renvoie ce nom (ex : "a3f9...7b2d.jpg")
    String enregistrer(MultipartFile fichier, String extension);

    // Renvoie le fichier rangé sous ce nom, pour pouvoir l'afficher
    Resource charger(String nomStocke);

    // Supprime le fichier rangé sous ce nom
    void supprimer(String nomStocke);
}