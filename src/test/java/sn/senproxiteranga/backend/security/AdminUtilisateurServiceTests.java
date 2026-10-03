package sn.senproxiteranga.backend.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import sn.senproxiteranga.backend.domain.AuthSession;
import sn.senproxiteranga.backend.domain.Role;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.enums.NomRole;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.dto.CreationUtilisateurRequest;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.UtilisateurMapper;
import sn.senproxiteranga.backend.repository.AuthSessionRepository;
import sn.senproxiteranga.backend.repository.RoleRepository;
import sn.senproxiteranga.backend.repository.UtilisateurRepository;
import sn.senproxiteranga.backend.repository.ZoneRepository;
import sn.senproxiteranga.backend.service.implementation.AuthServiceImpl;

import java.util.List;
import java.util.Optional;

class AdminUtilisateurServiceTests {

    private final UtilisateurRepository users = mock(UtilisateurRepository.class);
    private final RoleRepository roles = mock(RoleRepository.class);
    private final AuthSessionRepository sessions = mock(AuthSessionRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final AuthServiceImpl service =
            new AuthServiceImpl(
                    users,
                    mock(ZoneRepository.class),
                    new UtilisateurMapper(),
                    encoder,
                    roles,
                    sessions);

    private CreationUtilisateurRequest request(NomRole role) {
        return new CreationUtilisateurRequest(
                " Awa ",
                "Diop",
                "+221771234567",
                "Admin@example.com",
                "Password123!",
                true,
                role,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    @Test
    void adminIsSavedWithHashedPasswordAndStoredRole() {
        when(roles.findByNom(NomRole.ADMINISTRATEUR))
                .thenReturn(Optional.of(new Role(NomRole.ADMINISTRATEUR)));
        when(users.save(any(Utilisateur.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        var result = service.creerUtilisateur(request(NomRole.ADMINISTRATEUR));
        var captor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(users).save(captor.capture());
        assertEquals("ADMINISTRATEUR", result.role());
        assertEquals(StatutCompte.ACTIF, result.statutCompte());
        assertEquals("admin@example.com", result.email());
        assertEquals("771234567", result.telephone());
        assertTrue(encoder.matches("Password123!", captor.getValue().getMotDePasseHache()));
        assertNotNull(captor.getValue().getDateAcceptationCgu());
    }

    @Test
    void duplicateEmailDoesNotCreateAccount() {
        when(users.existsByEmailIgnoreCase("Admin@example.com")).thenReturn(true);
        assertThrows(
                BusinessException.class,
                () -> service.creerUtilisateur(request(NomRole.ADMINISTRATEUR)));
        verify(users, never()).save(any());
    }

    @Test
    void clientRoleUsesClientRegistration() {
        when(roles.findByNom(NomRole.CLIENT)).thenReturn(Optional.of(new Role(NomRole.CLIENT)));
        when(users.save(any(Utilisateur.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals("CLIENT", service.creerUtilisateur(request(NomRole.CLIENT)).role());
    }

    @Test
    void suspensionRevokesSessionsAndReactivationDoesNotRestoreThem() {
        var user = new Utilisateur();
        user.setRole(new Role(NomRole.CLIENT));
        var session = new AuthSession();
        when(users.findById(10L)).thenReturn(Optional.of(user));
        when(users.save(any(Utilisateur.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(sessions.findByUtilisateurIdAndRevokedFalse(10L)).thenReturn(List.of(session));
        var suspended = service.changerStatutCompte(10L, StatutCompte.SUSPENDU);
        assertEquals(StatutCompte.SUSPENDU, suspended.statutCompte());
        assertTrue(session.isRevoked());
        var active = service.changerStatutCompte(10L, StatutCompte.ACTIF);
        assertEquals(StatutCompte.ACTIF, active.statutCompte());
        assertTrue(session.isRevoked());
        verify(sessions, times(1)).findByUtilisateurIdAndRevokedFalse(10L);
    }

    @Test
    void unknownAccountIsNotFound() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> service.changerStatutCompte(10L, StatutCompte.SUSPENDU));
        verifyNoInteractions(sessions);
    }
}
