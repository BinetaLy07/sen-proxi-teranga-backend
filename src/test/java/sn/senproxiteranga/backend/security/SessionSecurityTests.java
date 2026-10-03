package sn.senproxiteranga.backend.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import sn.senproxiteranga.backend.domain.*;
import sn.senproxiteranga.backend.domain.enums.*;
import sn.senproxiteranga.backend.dto.ConnexionRequest;
import sn.senproxiteranga.backend.repository.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

class SessionSecurityTests {
    private final AuthSessionRepository sessions = mock(AuthSessionRepository.class);
    private final UtilisateurRepository users = mock(UtilisateurRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);

    private SessionService service() {
        SessionService service = new SessionService(sessions, users, encoder);
        ReflectionTestUtils.setField(service, "accessTtl", Duration.ofMinutes(15));
        ReflectionTestUtils.setField(service, "refreshTtl", Duration.ofDays(7));
        return service;
    }

    @Test
    void loginUsesStoredRoleAndStoresOnlyHashes() {
        Utilisateur user = new Utilisateur();
        user.setId(10L);
        user.setRole(new Role(NomRole.PROFESSIONNEL));
        user.setMotDePasseHache("encoded");
        when(users.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        when(encoder.matches("password", "encoded")).thenReturn(true);
        var tokens = service().login(new ConnexionRequest("test@example.com", "password"));
        var captor = org.mockito.ArgumentCaptor.forClass(AuthSession.class);
        verify(sessions).save(captor.capture());
        assertEquals("PROFESSIONNEL", tokens.role());
        assertEquals(SessionService.hash(tokens.accessToken()), captor.getValue().getAccessHash());
        assertNotEquals(tokens.refreshToken(), captor.getValue().getRefreshHash());
    }

    @Test
    void revokedAndExpiredSessionsAreRejected() {
        AuthSession session = new AuthSession();
        session.setRevoked(true);
        when(sessions.findByAccessHash(SessionService.hash("token")))
                .thenReturn(Optional.of(session));
        assertThrows(BadCredentialsException.class, () -> service().authenticate("token"));
        session.setRevoked(false);
        session.setAccessExpiresAt(Instant.now().minusSeconds(1));
        assertThrows(BadCredentialsException.class, () -> service().authenticate("token"));
    }

    @Test
    void refreshRotatesBothTokensAndKeepsSessionDeadline() {
        Utilisateur user = new Utilisateur();
        user.setId(10L);
        user.setRole(new Role(NomRole.CLIENT));
        AuthSession session = new AuthSession();
        session.setUtilisateur(user);
        Instant deadline = Instant.now().plusSeconds(3600);
        session.setExpiresAt(deadline);
        when(sessions.findByRefreshHash(SessionService.hash("old")))
                .thenReturn(Optional.of(session));
        var result = service().refresh("old");
        assertEquals(deadline, result.refreshExpiresAt());
        assertNotEquals("old", result.refreshToken());
        assertEquals(SessionService.hash(result.refreshToken()), session.getRefreshHash());
    }

    @Test
    void endpointsRejectOtherAccountsAndUnrelatedDemandes() {
        DemandeRepository demandes = mock(DemandeRepository.class);
        EndpointAccess access = new EndpointAccess(demandes, mock(MediaDemandeRepository.class));
        var auth =
                new UsernamePasswordAuthenticationToken(
                        new SessionPrincipal(10L, 1L, "CLIENT"), null);
        assertTrue(access.allowed(auth, "/api/clients/10/demandes", "GET"));
        assertFalse(access.allowed(auth, "/api/clients/11/demandes", "GET"));
        assertFalse(access.allowed(auth, "/api/professionnels/10/demandes", "GET"));
        when(demandes.findById(4L)).thenReturn(Optional.empty());
        assertFalse(access.allowed(auth, "/api/demandes/4/devis", "GET"));
    }

    @Test
    void mediaFilesAreRestrictedToDemandParticipants() {
        MediaDemandeRepository medias = mock(MediaDemandeRepository.class);
        EndpointAccess access = new EndpointAccess(mock(DemandeRepository.class), medias);
        Utilisateur client = new Utilisateur();
        client.setId(10L);
        Utilisateur pro = new Utilisateur();
        pro.setId(20L);
        Demande demande = new Demande();
        demande.setClient(client);
        demande.setProfessionnel(pro);
        MediaDemande media = new MediaDemande();
        media.setDemande(demande);
        when(medias.findById(3L)).thenReturn(Optional.of(media));
        for (long userId : new long[] {10L, 20L, 30L}) {
            var auth = new UsernamePasswordAuthenticationToken(
                    new SessionPrincipal(userId, 1L, "CLIENT"), null);
            assertEquals(userId != 30L,
                    access.allowed(auth, "/api/medias/3/fichier", "GET"));
            assertFalse(access.allowed(auth, "/api/medias/3/fichier", "POST"));
        }
    }
}
