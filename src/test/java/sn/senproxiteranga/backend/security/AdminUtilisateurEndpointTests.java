package sn.senproxiteranga.backend.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import sn.senproxiteranga.backend.config.SecurityConfig;
import sn.senproxiteranga.backend.controller.AdminUtilisateurController;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.dto.CreationUtilisateurRequest;
import sn.senproxiteranga.backend.service.AuthService;

@WebMvcTest(AdminUtilisateurController.class)
@Import(SecurityConfig.class)
class AdminUtilisateurEndpointTests {

    @Autowired private MockMvc mvc;

    @MockitoBean private AuthService authService;

    @MockitoBean private SessionService sessionService;

    @MockitoBean private EndpointAccess endpointAccess;

    private static final String BODY =
            """
            {"prenom":"Awa","nom":"Diop","telephone":"771234567",
             "email":"admin@example.com","motDePasse":"Password123!",
             "cguAcceptees":true,"role":"ADMINISTRATEUR"}
            """;

    @Test
    void anonymousCannotCreateAccount() throws Exception {
        mvc.perform(post("/api/admin/utilisateurs").contentType("application/json").content(BODY))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(authService);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void clientCannotCreateAccount() throws Exception {
        mvc.perform(post("/api/admin/utilisateurs").contentType("application/json").content(BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(authService);
    }

    @Test
    @WithMockUser(roles = "PROFESSIONNEL")
    void professionnelCannotCreateAccount() throws Exception {
        mvc.perform(post("/api/admin/utilisateurs").contentType("application/json").content(BODY))
                .andExpect(status().isForbidden());
        verifyNoInteractions(authService);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATEUR")
    void adminCanCreateAccount() throws Exception {
        mvc.perform(post("/api/admin/utilisateurs").contentType("application/json").content(BODY))
                .andExpect(status().isCreated());
        verify(authService).creerUtilisateur(any(CreationUtilisateurRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATEUR")
    void invalidRequestIsRejected() throws Exception {
        mvc.perform(
                        post("/api/admin/utilisateurs")
                                .contentType("application/json")
                                .content(BODY.replace("admin@example.com", "invalid-email")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(authService);
    }

    @Test
    void anonymousCannotChangeStatus() throws Exception {
        mvc.perform(
                        patch("/api/admin/utilisateurs/10/statut-compte")
                                .contentType("application/json")
                                .content("{\"statutCompte\":\"SUSPENDU\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(authService);
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void clientCannotSuspendAccount() throws Exception {
        mvc.perform(
                        patch("/api/admin/utilisateurs/10/statut-compte")
                                .contentType("application/json")
                                .content("{\"statutCompte\":\"SUSPENDU\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(authService);
    }

    @Test
    @WithMockUser(roles = "PROFESSIONNEL")
    void professionnelCannotSuspendAccount() throws Exception {
        mvc.perform(
                        patch("/api/admin/utilisateurs/10/statut-compte")
                                .contentType("application/json")
                                .content("{\"statutCompte\":\"SUSPENDU\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(authService);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATEUR")
    void adminCanSuspendAndReactivate() throws Exception {
        for (var compteStatut : StatutCompte.values()) {
            mvc.perform(
                            patch("/api/admin/utilisateurs/10/statut-compte")
                                    .contentType("application/json")
                                    .content("{\"statutCompte\":\"" + compteStatut.name() + "\"}"))
                    .andExpect(status().isOk());
            verify(authService).changerStatutCompte(10L, compteStatut);
        }
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATEUR")
    void missingStatusIsRejected() throws Exception {
        mvc.perform(
                        patch("/api/admin/utilisateurs/10/statut-compte")
                                .contentType("application/json")
                                .content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(authService);
    }
}
