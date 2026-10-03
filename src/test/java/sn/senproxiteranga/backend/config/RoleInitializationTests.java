package sn.senproxiteranga.backend.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.validation.Validation;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import sn.senproxiteranga.backend.domain.Role;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.enums.NomRole;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.repository.RoleRepository;
import sn.senproxiteranga.backend.repository.UtilisateurRepository;

import java.util.EnumMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

class RoleInitializationTests {

    private final RoleRepository roles = mock(RoleRepository.class);
    private final UtilisateurRepository users = mock(UtilisateurRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final EnumMap<NomRole, Role> storedRoles = new EnumMap<>(NomRole.class);
    private final AtomicReference<Utilisateur> storedAdmin = new AtomicReference<>();

    private RoleInitialization runner(String password) {
        when(roles.findByNom(any()))
                .thenAnswer(
                        invocation ->
                                Optional.ofNullable(storedRoles.get(invocation.getArgument(0))));
        when(roles.save(any(Role.class)))
                .thenAnswer(
                        invocation -> {
                            Role role = invocation.getArgument(0);
                            storedRoles.put(role.getNom(), role);
                            return role;
                        });
        when(users.findByEmailIgnoreCase("admin@example.com"))
                .thenAnswer(invocation -> Optional.ofNullable(storedAdmin.get()));
        when(users.save(any(Utilisateur.class)))
                .thenAnswer(
                        invocation -> {
                            Utilisateur user = invocation.getArgument(0);
                            storedAdmin.set(user);
                            return user;
                        });
        return new RoleInitialization(
                roles,
                users,
                encoder,
                new BootstrapAdminProperties(
                        "admin@example.com", "+221770000000", "Admin", "Teranga", password),
                Validation.buildDefaultValidatorFactory().getValidator());
    }

    @Test
    void initializesOnceAndRecreatesDeletedAccountOrRole() {
        var initialization = runner("TestPassword123!");
        initialization.run(null);
        var admin = storedAdmin.get();
        assertEquals(3, storedRoles.size());
        assertTrue(admin.aRole(NomRole.ADMINISTRATEUR));
        assertEquals(StatutCompte.ACTIF, admin.getStatutCompte());
        assertEquals("770000000", admin.getTelephone());
        assertTrue(encoder.matches("TestPassword123!", admin.getMotDePasseHache()));
        initialization.run(null);
        verify(users, times(1)).save(any());
        verify(roles, times(3)).save(any());
        assertSame(admin, storedAdmin.get());

        storedAdmin.set(null);
        storedRoles.remove(NomRole.ADMINISTRATEUR);
        initialization.run(null);
        verify(users, times(2)).save(any());
        verify(roles, times(4)).save(any());
        assertNotSame(admin, storedAdmin.get());
        assertTrue(storedAdmin.get().aRole(NomRole.ADMINISTRATEUR));
    }

    @Test
    void existingSuspendedAdminIsUnchangedEvenWithoutConfiguredPassword() {
        var admin = new Utilisateur();
        admin.setRole(new Role(NomRole.ADMINISTRATEUR));
        admin.setStatutCompte(StatutCompte.SUSPENDU);
        admin.setMotDePasseHache("existing-hash");
        storedAdmin.set(admin);
        runner("").run(null);
        verify(users, never()).save(any());
        assertEquals(StatutCompte.SUSPENDU, admin.getStatutCompte());
        assertEquals("existing-hash", admin.getMotDePasseHache());
    }

    @Test
    void missingPasswordFailsWhenAdminIsAbsent() {
        assertThrows(IllegalStateException.class, () -> runner("").run(null));
        verify(users, never()).save(any());
    }

    @Test
    void existingClientWithConfiguredEmailIsNeverPromoted() {
        var client = new Utilisateur();
        client.setRole(new Role(NomRole.CLIENT));
        storedAdmin.set(client);
        assertThrows(IllegalStateException.class, () -> runner("TestPassword123!").run(null));
        assertTrue(client.aRole(NomRole.CLIENT));
        verify(users, never()).save(any());
    }

    @Test
    void occupiedTelephonePreventsDuplicateCreation() {
        var initialization = runner("TestPassword123!");
        when(users.existsByTelephone("770000000")).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> initialization.run(null));
        verify(users, never()).save(any());
    }
}
