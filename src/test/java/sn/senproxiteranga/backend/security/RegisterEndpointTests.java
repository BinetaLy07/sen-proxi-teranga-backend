package sn.senproxiteranga.backend.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import sn.senproxiteranga.backend.config.SecurityConfig;
import sn.senproxiteranga.backend.controller.AuthController;
import sn.senproxiteranga.backend.dto.RegisterRequest;
import sn.senproxiteranga.backend.service.AuthService;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class RegisterEndpointTests {
    @Autowired private MockMvc mvc;
    @MockitoBean private AuthService authService;
    @MockitoBean private SessionService sessionService;
    @MockitoBean private EndpointAccess endpointAccess;

    private String body(String role, String extra) {
        return """
        {"prenom":"Awa","nom":"Diop","telephone":"771234567",
         "email":"awa@example.com","motDePasse":"Password123!",
         "cguAcceptees":true,"role":"%s"%s}
        """
                .formatted(role, extra);
    }

    @Test
    void registerClientIsPublic() throws Exception {
        mvc.perform(
                        post("/api/auth/register")
                                .contentType("application/json")
                                .content(body("CLIENT", "")))
                .andExpect(status().isCreated());
        verify(authService).register(any(RegisterRequest.class));
        verifyNoInteractions(sessionService);
    }

    @Test
    void registerProfessionnelIsPublic() throws Exception {
        mvc.perform(
                        post("/api/auth/register")
                                .contentType("application/json")
                                .content(body("PROFESSIONNEL", ",\"metier\":\"Plombier\"")))
                .andExpect(status().isCreated());
        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void adminRegistrationIsRejected() throws Exception {
        mvc.perform(
                        post("/api/auth/register")
                                .contentType("application/json")
                                .content(body("ADMINISTRATEUR", "")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(authService);
    }

    @Test
    void professionnelRequiresMetier() throws Exception {
        mvc.perform(
                        post("/api/auth/register")
                                .contentType("application/json")
                                .content(body("PROFESSIONNEL", "")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(authService);
    }

    @Test
    void sessionsRemainProtected() throws Exception {
        mvc.perform(get("/api/auth/sessions")).andExpect(status().isUnauthorized());
        verifyNoInteractions(sessionService);
    }
}
