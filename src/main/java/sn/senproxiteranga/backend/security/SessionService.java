package sn.senproxiteranga.backend.security;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.senproxiteranga.backend.domain.AuthSession;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.dto.ConnexionRequest;
import sn.senproxiteranga.backend.dto.TokenResponse;
import sn.senproxiteranga.backend.mapper.UtilisateurMapper;
import sn.senproxiteranga.backend.repository.AuthSessionRepository;
import sn.senproxiteranga.backend.repository.UtilisateurRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionService {
    private final AuthSessionRepository sessions;
    private final UtilisateurRepository users;
    private final PasswordEncoder encoder;
    private final UtilisateurMapper utilisateurMapper;

    @Value("${security.access-ttl:PT15M}")
    private Duration accessTtl;

    @Value("${security.refresh-ttl:P7D}")
    private Duration refreshTtl;

    private final SecureRandom random = new SecureRandom();
    private static final String DUMMY =
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                    .encode("dummy-password");

    // Connexion avec le téléphone OU l'email (un « @ » = un email, sinon un numéro)
    public TokenResponse login(ConnexionRequest r) {
        String identifiant = r.identifiant().trim();
        var user = (identifiant.contains("@")
                        ? users.findByEmailIgnoreCase(identifiant)
                        : users.findByTelephone(utilisateurMapper.normaliserTelephone(identifiant)))
                .orElse(null);
        boolean matches =
                encoder.matches(r.motDePasse(), user == null ? DUMMY : user.getMotDePasseHache());
        if (user == null || !matches || user.getStatutCompte() != StatutCompte.ACTIF) {
            throw invalid();
        }
        // Jamais connecté avant = 1re connexion après l'inscription (fenêtre de bienvenue)
        boolean premiereConnexion = user.getDernierAcces() == null;
        user.setDernierAcces(LocalDateTime.now());

        var session = new AuthSession();
        session.setUtilisateur(user);
        session.setExpiresAt(Instant.now().plus(refreshTtl));
        return rotate(session, premiereConnexion);
    }

    public TokenResponse refresh(String token) {
        var s = sessions.findByRefreshHash(hash(token)).orElseThrow(SessionService::invalid);
        if (s.isRevoked()
                || !s.getExpiresAt().isAfter(Instant.now())
                || s.getUtilisateur().getStatutCompte() != StatutCompte.ACTIF) {
            throw invalid();
        }
        return rotate(s, false);
    }

    @Transactional(readOnly = true)
    public SessionPrincipal authenticate(String token) {
        var s = sessions.findByAccessHash(hash(token)).orElseThrow(SessionService::invalid);
        if (s.isRevoked()
                || !s.getAccessExpiresAt().isAfter(Instant.now())
                || !s.getExpiresAt().isAfter(Instant.now())
                || s.getUtilisateur().getStatutCompte() != StatutCompte.ACTIF) {
            throw invalid();
        }
        return new SessionPrincipal(
                s.getUtilisateur().getId(), s.getId(), role(s.getUtilisateur()));
    }

    public void logout(SessionPrincipal p) {
        sessions.findById(p.sessionId()).ifPresent(s -> s.setRevoked(true));
    }

    public void logoutAll(SessionPrincipal p) {
        sessions.findByUtilisateurIdAndRevokedFalse(p.utilisateurId())
                .forEach(s -> s.setRevoked(true));
    }

    @Transactional(readOnly = true)
    public List<SessionInfo> list(SessionPrincipal p) {
        return sessions.findByUtilisateurIdAndRevokedFalse(p.utilisateurId()).stream()
                .filter(s -> s.getExpiresAt().isAfter(Instant.now()))
                .map(
                        s ->
                                new SessionInfo(
                                        s.getId(),
                                        s.getExpiresAt(),
                                        s.getId().equals(p.sessionId())))
                .toList();
    }

    public void revoke(SessionPrincipal p, Long id) {
        var s = sessions.findById(id).orElseThrow(SessionService::invalid);
        if (!s.getUtilisateur().getId().equals(p.utilisateurId())) {
            throw new org.springframework.security.access.AccessDeniedException("Accès interdit");
        }
        s.setRevoked(true);
    }

    public record SessionInfo(Long id, Instant expiresAt, boolean current) {}

    private TokenResponse rotate(AuthSession s, boolean premiereConnexion) {
        String a = token();
        String r = token();
        s.setAccessHash(hash(a));
        s.setRefreshHash(hash(r));
        Instant expiration = Instant.now().plus(accessTtl);
        s.setAccessExpiresAt(expiration.isBefore(s.getExpiresAt()) ? expiration : s.getExpiresAt());
        sessions.save(s);
        return new TokenResponse(
                a,
                r,
                "Bearer",
                s.getAccessExpiresAt(),
                s.getExpiresAt(),
                s.getUtilisateur().getId(),
                role(s.getUtilisateur()),
                s.getUtilisateur().getPrenom(),
                s.getUtilisateur().getNom(),
                premiereConnexion);
    }

    static String role(Utilisateur utilisateur) {
        if (utilisateur.getRole() == null) {
            throw invalid();
        }
        return utilisateur.getRole().getNom().name();
    }

    private String token() {
        byte[] b = new byte[32];
        random.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    public static String hash(String token) {
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static BadCredentialsException invalid() {
        return new BadCredentialsException("Identifiants ou token invalides");
    }
}
