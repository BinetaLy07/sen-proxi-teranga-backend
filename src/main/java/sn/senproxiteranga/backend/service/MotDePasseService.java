package sn.senproxiteranga.backend.service;

import sn.senproxiteranga.backend.dto.ChangerMotDePasseRequest;

public interface MotDePasseService {

    // Changer son mot de passe (utilisateur connecté).
    // Ferme les autres connexions et renvoie combien ont été fermées.
    int changer(Long utilisateurId, Long sessionActuelleId, ChangerMotDePasseRequest request);
}