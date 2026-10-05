package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.senproxiteranga.backend.domain.AuthSession;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.dto.ChangerMotDePasseRequest;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.repository.AuthSessionRepository;
import sn.senproxiteranga.backend.repository.UtilisateurRepository;
import sn.senproxiteranga.backend.service.MotDePasseService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MotDePasseServiceImpl implements MotDePasseService {

    private final UtilisateurRepository utilisateurRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordEncoder passwordEncoder;

    // =====================================================================
    //                    CHANGER MON MOT DE PASSE
    // =====================================================================

    @Override
    public int changer(Long utilisateurId, Long sessionActuelleId, ChangerMotDePasseRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + utilisateurId));

        // Règle 1 : il faut connaître l'ancien mot de passe
        // (on compare avec la version hachée : on ne peut jamais "relire" un mot de passe)
        if (!passwordEncoder.matches(request.ancienMotDePasse(), utilisateur.getMotDePasseHache())) {
            throw new BusinessException("L'ancien mot de passe est incorrect");
        }

        // Règle 3 : le nouveau doit être différent de l'ancien
        if (passwordEncoder.matches(request.nouveauMotDePasse(), utilisateur.getMotDePasseHache())) {
            throw new BusinessException("Le nouveau mot de passe doit être différent de l'ancien");
        }

        // Le nouveau mot de passe est enregistré HACHÉ, jamais en clair
        utilisateur.setMotDePasseHache(passwordEncoder.encode(request.nouveauMotDePasse()));

        // Règle 4 : on ferme toutes les AUTRES connexions (autres téléphones, ordinateurs).
        // La connexion actuelle reste ouverte.
        List<AuthSession> autresConnexions = authSessionRepository
                .findByUtilisateurIdAndRevokedFalse(utilisateurId).stream()
                .filter(session -> !session.getId().equals(sessionActuelleId))
                .toList();
        autresConnexions.forEach(session -> session.setRevoked(true));

        return autresConnexions.size();
    }
}