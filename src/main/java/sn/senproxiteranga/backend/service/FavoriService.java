package sn.senproxiteranga.backend.service;

import sn.senproxiteranga.backend.dto.FavoriResponse;

import java.util.List;

/**
 * Les professionnels favoris d'un client.
 */
public interface FavoriService {

    // Le client ajoute un professionnel à ses favoris
    FavoriResponse ajouter(Long clientId, Long professionnelId);

    // Le client retire un professionnel de ses favoris
    void retirer(Long clientId, Long professionnelId);

    // La liste des favoris du client
    List<FavoriResponse> lister(Long clientId);
}