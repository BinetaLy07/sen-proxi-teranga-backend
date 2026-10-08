package sn.senproxiteranga.backend.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import sn.senproxiteranga.backend.dto.ContactProResponse;
import sn.senproxiteranga.backend.dto.ModifierProfilRequest;
import sn.senproxiteranga.backend.dto.ProfessionnelResumeResponse;
import sn.senproxiteranga.backend.dto.ProfilProfessionnelResponse;
import sn.senproxiteranga.backend.dto.RealisationResponse;

import java.util.List;

/**
 * Recherche, profil public et espace profil des professionnels.
 */
public interface ProfessionnelService {

    // ===================== Visiteurs et clients =====================

    // Recherche avec filtres facultatifs : métier, zone, catégorie
    List<ProfessionnelResumeResponse> rechercher(String metier, Long zoneId, Long categorieId);

    // Profil complet d'un professionnel validé (visible par tous)
    ProfilProfessionnelResponse profilPublic(Long professionnelId);

    // Téléphone et WhatsApp d'un professionnel validé (seulement pour une personne connectée)
    ContactProResponse contact(Long professionnelId);

    // ===================== Espace du professionnel =====================

    // Son propre profil, même s'il n'est pas encore validé
    ProfilProfessionnelResponse monProfil(Long professionnelId);

    // Modifier description, compétences, expérience, WhatsApp, option SMS
    ProfilProfessionnelResponse modifierProfil(Long professionnelId, ModifierProfilRequest request);

    // Changer la photo de profil (JPG ou PNG)
    ProfilProfessionnelResponse changerPhoto(Long professionnelId, MultipartFile photo);

    // Ajouter une réalisation à sa galerie (photo + titre + description facultative)
    RealisationResponse ajouterRealisation(Long professionnelId, MultipartFile photo,
                                           String titre, String description);

    // Retirer une réalisation de sa galerie
    void supprimerRealisation(Long professionnelId, Long realisationId);

    // ===================== Affichage des images =====================

    // Les images d'un pro validé sont visibles par tous.
    // Celles d'un pro pas encore validé : seulement par lui-même et par l'administrateur.
    // demandeurId vaut null pour un visiteur non connecté.
    FichierImage chargerPhoto(Long professionnelId, Long demandeurId, boolean demandeurAdmin);

    FichierImage chargerRealisation(Long realisationId, Long demandeurId, boolean demandeurAdmin);

    /**
     * Une image prête à être envoyée au navigateur.
     */
    record FichierImage(Resource ressource, String contentType) {
    }
}