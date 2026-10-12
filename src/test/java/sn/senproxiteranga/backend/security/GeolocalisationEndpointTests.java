package sn.senproxiteranga.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sn.senproxiteranga.backend.config.SecurityConfig;
import sn.senproxiteranga.backend.controller.GeolocalisationController;
import sn.senproxiteranga.backend.service.GeolocalisationService;


import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GeolocalisationController.class)
@Import(SecurityConfig.class)
class GeolocalisationEndpointTests {
    @Autowired private MockMvc mvc;
    @MockitoBean private GeolocalisationService geolocalisation;
    @MockitoBean private SessionService sessions;
    @MockitoBean private EndpointAccess access;

    @Test
    void proximityRequiresClientRole() throws Exception {
        String url = "/api/proximite/professionnels?latitude=14.7&longitude=-17.4";
        mvc.perform(get(url)).andExpect(status().isUnauthorized());
        mvc.perform(get(url).header("Authorization", token("PROFESSIONNEL"))).andExpect(status().isForbidden());
        mvc.perform(get(url).header("Authorization", token("CLIENT"))).andExpect(status().isOk());
        verify(geolocalisation).rechercher(14.7, -17.4, 10, null, null, null);
    }

    @Test
    void positionWriteChecksOwnerAndValidatesCoordinates() throws Exception {
        when(access.allowed(any(), eq("/api/professionnels/5/position"), eq("PUT"))).thenReturn(true);
        mvc.perform(put("/api/professionnels/5/position").header("Authorization", token("PROFESSIONNEL"))
                        .contentType("application/json").content("{\"latitude\":91,\"longitude\":0}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/professionnels/6/position").header("Authorization", token("PROFESSIONNEL"))
                        .contentType("application/json").content("{\"latitude\":0,\"longitude\":0}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(geolocalisation);
    }

    private String token(String role) {
        when(sessions.authenticate(role)).thenReturn(new SessionPrincipal(5L, 1L, role));
        return "Bearer " + role;
    }
}
