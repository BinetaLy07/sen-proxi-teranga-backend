package sn.senproxiteranga.backend.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.senproxiteranga.backend.domain.AuthSession;
import sn.senproxiteranga.backend.domain.CodeReinitialisation;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.dto.ChangerMotDePasseRequest;
import sn.senproxiteranga.backend.dto.ReinitialiserMotDePasseRequest;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.UtilisateurMapper;
import sn.senproxiteranga.backend.repository.AuthSessionRepository;
import sn.senproxiteranga.backend.repository.CodeReinitialisationRepository;
import sn.senproxiteranga.backend.repository.UtilisateurRepository;
import sn.senproxiteranga.backend.service.MotDePasseService;
import sn.senproxiteranga.backend.service.SmsService;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MotDePasseServiceImpl implements MotDePasseService {

    private static final int DUREE_VALIDITE_CODE_MINUTES = 10;
    private static final int MAX_TENTATIVES = 5;
    private static final int DELAI_ENTRE_DEUX_SMS_SECONDES = 60;

    // Message volontairement vague : on ne dit jamais si le numéro existe ou pas
    private static final String CODE_INVALIDE = "Code incorrect ou expiré : demandez un nouveau code";

    // Générateur de nombres au hasard fait pour la sécurité (imprévisible)
    private final SecureRandom hasard = new SecureRandom();

    private final UtilisateurRepository utilisateurRepository;
    private final AuthSessionRepository authSessionRepository;
    private final CodeReinitialisationRepository codeRepository;
    private final UtilisateurMapper utilisateurMapper;
    private final PasswordEncoder passwordEncoder;
    private final SmsService smsService;

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

    // =====================================================================
    //              MOT DE PASSE OUBLIÉ - ÉTAPE 1 : LE CODE PAR SMS
    // =====================================================================

    @Override
    public void demanderCode(String telephone) {
        Optional<Utilisateur> trouve = utilisateurRepository
                .findByTelephone(utilisateurMapper.normaliserTelephone(telephone));

        // R1 : numéro inconnu ou compte suspendu -> on ne fait rien, mais SANS le dire
        if (trouve.isEmpty() || trouve.get().getStatutCompte() != StatutCompte.ACTIF) {
            return;
        }
        Utilisateur utilisateur = trouve.get();

        // R4 : pas plus d'un SMS par minute
        Optional<CodeReinitialisation> dernier = codeRepository
                .findFirstByUtilisateurIdAndUtiliseFalseOrderByCreatedAtDescIdDesc(utilisateur.getId());
        if (dernier.isPresent() && dernier.get().getCreatedAt()
                .isAfter(LocalDateTime.now().minusSeconds(DELAI_ENTRE_DEUX_SMS_SECONDES))) {
            return;
        }

        // R3 : un nouveau code annule tous les anciens
        codeRepository.findByUtilisateurIdAndUtiliseFalse(utilisateur.getId())
                .forEach(ancien -> ancien.setUtilise(true));

        // R2 : un code à 6 chiffres tiré au hasard (de 000000 à 999999)
        String code = String.format("%06d", hasard.nextInt(1_000_000));

        CodeReinitialisation nouveau = new CodeReinitialisation();
        nouveau.setUtilisateur(utilisateur);
        nouveau.setCodeHache(passwordEncoder.encode(code));          // haché comme un mot de passe
        nouveau.setExpireLe(LocalDateTime.now().plusMinutes(DUREE_VALIDITE_CODE_MINUTES)); // R5
        codeRepository.save(nouveau);

        smsService.envoyer(utilisateur.getTelephone(),
                "Sen Proxi Teranga : votre code de reinitialisation est " + code
                        + ". Valable " + DUREE_VALIDITE_CODE_MINUTES + " minutes. Ne le donnez a personne.");
    }

    // =====================================================================
    //          MOT DE PASSE OUBLIÉ - ÉTAPE 2 : NOUVEAU MOT DE PASSE
    // =====================================================================

    // noRollbackFor : même quand on refuse un code faux, le compteur d'essais
    // doit rester enregistré (sinon un robot aurait des essais illimités)
    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public void reinitialiser(ReinitialiserMotDePasseRequest request) {
        Utilisateur utilisateur = utilisateurRepository
                .findByTelephone(utilisateurMapper.normaliserTelephone(request.telephone()))
                .orElseThrow(() -> new BusinessException(CODE_INVALIDE));          // R1

        CodeReinitialisation code = codeRepository
                .findFirstByUtilisateurIdAndUtiliseFalseOrderByCreatedAtDescIdDesc(utilisateur.getId())
                .orElseThrow(() -> new BusinessException(CODE_INVALIDE));          // R7

        // R5 : code expiré
        if (code.getExpireLe().isBefore(LocalDateTime.now())) {
            code.setUtilise(true);
            throw new BusinessException(CODE_INVALIDE);
        }

        // R6 : code faux -> un essai de moins ; au 5e, le code est grillé
        if (!passwordEncoder.matches(request.code(), code.getCodeHache())) {
            code.setTentatives(code.getTentatives() + 1);
            int restants = MAX_TENTATIVES - code.getTentatives();
            if (restants <= 0) {
                code.setUtilise(true);
                throw new BusinessException("Trop d'essais : ce code ne marche plus, demandez-en un nouveau");
            }
            throw new BusinessException("Code incorrect (" + restants + " essai(s) restant(s))");
        }

        // Tout est bon : le code est consommé (R7) et le mot de passe changé (haché)
        code.setUtilise(true);
        utilisateur.setMotDePasseHache(passwordEncoder.encode(request.nouveauMotDePasse()));

        // R8 : toutes les connexions sont fermées, et le propriétaire est prévenu par SMS
        authSessionRepository.findByUtilisateurIdAndRevokedFalse(utilisateur.getId())
                .forEach(session -> session.setRevoked(true));
        smsService.envoyer(utilisateur.getTelephone(),
                "Sen Proxi Teranga : votre mot de passe vient d'etre modifie. "
                        + "Si ce n'est pas vous, contactez-nous immediatement.");
    }
}