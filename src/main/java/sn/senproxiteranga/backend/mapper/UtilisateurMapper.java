package sn.senproxiteranga.backend.mapper;

import org.springframework.stereotype.Component;

import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.enums.NomRole;
import sn.senproxiteranga.backend.domain.enums.StatutVerification;
import sn.senproxiteranga.backend.dto.InscriptionClientRequest;
import sn.senproxiteranga.backend.dto.InscriptionProfessionnelRequest;
import sn.senproxiteranga.backend.dto.UtilisateurResponse;

@Component
public class UtilisateurMapper {

    // Formulaire d'inscription -> nouveau Utilisateur (mot de passe et zone : ajoutés par le
    // service)
    public Utilisateur toClient(InscriptionClientRequest request) {
        Utilisateur client = new Utilisateur();
        remplirCommun(
                client, request.prenom(), request.nom(), request.telephone(), request.email());
        client.setAdresse(request.adresse());
        return client;
    }

    // Formulaire d'inscription -> nouveau Utilisateur (mot de passe et zones : ajoutés par le
    // service)
    public Utilisateur toProfessionnel(InscriptionProfessionnelRequest request) {
        Utilisateur pro = new Utilisateur();
        remplirCommun(pro, request.prenom(), request.nom(), request.telephone(), request.email());
        pro.setStatutVerification(StatutVerification.EN_ATTENTE);
        pro.setMetier(request.metier().trim());
        pro.setCompetences(request.competences());
        pro.setDescription(request.description());
        pro.setWhatsapp(
                request.whatsapp() != null ? normaliserTelephone(request.whatsapp()) : null);
        return pro;
    }

    // N'importe quel utilisateur -> réponse (SANS le mot de passe)
    public UtilisateurResponse toResponse(Utilisateur utilisateur) {
        return new UtilisateurResponse(
                utilisateur.getId(),
                utilisateur.getPrenom(),
                utilisateur.getNom(),
                utilisateur.getTelephone(),
                utilisateur.getEmail(),
                roleDe(utilisateur),
                utilisateur.getStatutCompte(),
                utilisateur.aRole(NomRole.PROFESSIONNEL)
                        ? utilisateur.getStatutVerification()
                        : null,
                utilisateur.getCreatedAt());
    }

    // Garde le numéro sans l'indicatif +221 : "+221771234567" -> "771234567"
    public String normaliserTelephone(String telephone) {
        String t = telephone.trim();
        return t.startsWith("+221") ? t.substring(4) : t;
    }

    // ---------- Méthodes internes ----------

    private void remplirCommun(
            Utilisateur u, String prenom, String nom, String telephone, String email) {
        u.setPrenom(prenom.trim());
        u.setNom(nom.trim());
        u.setTelephone(normaliserTelephone(telephone));
        u.setEmail(email.trim().toLowerCase());
    }

    private String roleDe(Utilisateur utilisateur) {
        return utilisateur.getRole().getNom().name();
    }
}
