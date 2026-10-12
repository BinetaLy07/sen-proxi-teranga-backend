package sn.senproxiteranga.backend.security;

import org.junit.jupiter.api.Test;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.enums.*;
import sn.senproxiteranga.backend.dto.PositionRequest;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.mapper.ProfessionnelMapper;
import sn.senproxiteranga.backend.repository.*;
import sn.senproxiteranga.backend.service.GeolocalisationService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GeolocalisationTests {

    private final UtilisateurRepository utilisateurs = mock(UtilisateurRepository.class);
    private final GeolocalisationService service = new GeolocalisationService(
            utilisateurs, mock(AvisRepository.class), new ProfessionnelMapper());

    @Test
    void distanceHandlesSamePointAndInternationalDateLine() {
        assertEquals(0, GeolocalisationService.distanceKm(14.7, -17.4, 14.7, -17.4), 0.0001);
        assertEquals(111.195, GeolocalisationService.distanceKm(0, 0, 0, 1), 0.001);
        assertEquals(222.390, GeolocalisationService.distanceKm(0, 179, 0, -179), 0.001);
    }

    @Test
    void searchExcludesMissingCoordinatesAndOutsideRadiusAndSortsByDistance() {
        when(utilisateurs.rechercher("plombier", null, 2L, StatutVerification.VALIDE, StatutCompte.ACTIF))
                .thenReturn(List.of(pro(1, 0.04), pro(2, null), pro(3, 0.01), pro(4, 1.0)));
        var proches = service.rechercher(0, 0, 5, " plombier ", null, 2L);
        assertEquals(List.of(3L, 1L), proches.stream().map(p -> p.professionnel().id()).toList());
        assertEquals(1.112, proches.getFirst().distanceKm(), 0.001);
    }

    @Test
    void rejectsNonFiniteCoordinatesAndInvalidRadiusBeforeDatabaseAccess() {
        assertThrows(BusinessException.class, () -> service.rechercher(Double.NaN, 0, 10, null, null, null));
        assertThrows(BusinessException.class, () -> service.rechercher(91, 0, 10, null, null, null));
        assertThrows(BusinessException.class, () -> service.rechercher(0, 181, 10, null, null, null));
        assertThrows(BusinessException.class, () -> service.rechercher(0, 0, 101, null, null, null));
        verifyNoInteractions(utilisateurs);
    }

    @Test
    void professionalCanSetZeroCoordinatesAndRemovePublicLocation() {
        Utilisateur pro = pro(5, null);
        when(utilisateurs.findByIdAndRoleNom(5L, NomRole.PROFESSIONNEL)).thenReturn(Optional.of(pro));
        service.enregistrer(5L, new PositionRequest(0.0, 0.0));
        assertEquals(0, service.position(5L).latitude());
        service.supprimer(5L);
        assertNull(pro.getLatitude());
        assertNull(pro.getLongitude());
    }

    @Test
    void locationWriteIsRestrictedToTheProfessionalAccount() {
        EndpointAccess access = new EndpointAccess(mock(DemandeRepository.class), mock(MediaDemandeRepository.class));
        var auth = new UsernamePasswordAuthenticationToken(new SessionPrincipal(5L, 1L, "PROFESSIONNEL"), null);
        assertTrue(access.allowed(auth, "/api/professionnels/5/position", "PUT"));
        assertFalse(access.allowed(auth, "/api/professionnels/6/position", "PUT"));
        assertFalse(access.allowed(auth, "/api/professionnels/6/position", "DELETE"));
    }

    private Utilisateur pro(long id, Double longitude) {
        Utilisateur pro = new Utilisateur();
        pro.setId(id);
        pro.setPrenom("Awa");
        pro.setNom("Diop");
        pro.setLatitude(longitude == null ? null : 0.0);
        pro.setLongitude(longitude);
        return pro;
    }
}
