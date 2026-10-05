package sn.senproxiteranga.backend.service;

// Envoi de SMS. Aujourd'hui : simulation (SmsServiceSimule).
// Plus tard : une autre implémentation branchée sur un vrai fournisseur (ex : Orange SMS API),
// sans rien changer au reste du code.
public interface SmsService {

    void envoyer(String numero, String contenu);
}