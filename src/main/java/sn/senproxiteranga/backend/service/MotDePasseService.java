package sn.senproxiteranga.backend.service;

import sn.senproxiteranga.backend.dto.ChangerMotDePasseRequest;
import sn.senproxiteranga.backend.dto.ReinitialiserMotDePasseRequest;

public interface MotDePasseService {

    // Changer son mot de passe (utilisateur connecté).
    // Ferme les autres connexions et renvoie combien ont été fermées.
    int changer(Long utilisateurId, Long sessionActuelleId, ChangerMotDePasseRequest request);

    // Mot de passe oublié, étape 1 : envoyer un code par SMS (si le numéro correspond à un compte)
    void demanderCode(String telephone);

    // Mot de passe oublié, étape 2 : vérifier le code et enregistrer le nouveau mot de passe
    void reinitialiser(ReinitialiserMotDePasseRequest request);
}