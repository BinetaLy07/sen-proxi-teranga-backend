package sn.senproxiteranga.backend.security;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sn.senproxiteranga.backend.repository.DemandeRepository;

@Component
@RequiredArgsConstructor
public class EndpointAccess {
    private final DemandeRepository demandes;

    @Transactional(readOnly = true)
    public boolean allowed(Authentication auth, String path, String method) {
        if (auth == null || !(auth.getPrincipal() instanceof SessionPrincipal p)) {
            return false;
        }
        if (p.role().equals("ADMINISTRATEUR")) {
            return true;
        }
        String[] parts = path.split("/");
        try {
            if (parts.length >= 4 && parts[2].equals("clients")) {
                return p.role().equals("CLIENT")
                        && p.utilisateurId().equals(Long.valueOf(parts[3]));
            }
            if (parts.length >= 4 && parts[2].equals("professionnels")) {
                return p.role().equals("PROFESSIONNEL")
                        && p.utilisateurId().equals(Long.valueOf(parts[3]));
            }
            if (parts.length >= 4 && parts[2].equals("demandes")) {
                return demandes.findById(Long.valueOf(parts[3]))
                        .map(
                                d ->
                                        d.getClient().getId().equals(p.utilisateurId())
                                                || d.getProfessionnel()
                                                        .getId()
                                                        .equals(p.utilisateurId()))
                        .orElse(false);
            }
        } catch (NumberFormatException ex) {
            return false;
        }
        return false;
    }
}
