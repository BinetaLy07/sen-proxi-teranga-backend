package sn.senproxiteranga.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.senproxiteranga.backend.domain.Utilisateur;
import sn.senproxiteranga.backend.domain.enums.NomRole;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.domain.enums.StatutVerification;
import sn.senproxiteranga.backend.dto.*;
import sn.senproxiteranga.backend.exception.BusinessException;
import sn.senproxiteranga.backend.exception.ResourceNotFoundException;
import sn.senproxiteranga.backend.mapper.ProfessionnelMapper;
import sn.senproxiteranga.backend.repository.AvisRepository;
import sn.senproxiteranga.backend.repository.UtilisateurRepository;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GeolocalisationService {

    private final UtilisateurRepository utilisateurs;
    private final AvisRepository avis;
    private final ProfessionnelMapper mapper;

    @Transactional(readOnly = true)
    public PositionResponse position(Long id) {
        Utilisateur pro = professionnel(id);
        return new PositionResponse(pro.getLatitude(), pro.getLongitude());
    }

    public PositionResponse enregistrer(Long id, PositionRequest position) {
        verifier(position.latitude(), position.longitude(), 10);
        Utilisateur pro = professionnel(id);
        pro.setLatitude(position.latitude());
        pro.setLongitude(position.longitude());
        return new PositionResponse(pro.getLatitude(), pro.getLongitude());
    }

    public void supprimer(Long id) {
        Utilisateur pro = professionnel(id);
        pro.setLatitude(null);
        pro.setLongitude(null);
    }

    @Transactional(readOnly = true)
    public List<ProfessionnelProcheResponse> rechercher(
            double latitude, double longitude, double rayonKm,
            String metier, Long zoneId, Long categorieId) {
        verifier(latitude, longitude, rayonKm);
        String filtre = metier == null || metier.isBlank() ? null : metier.trim();
        return utilisateurs.rechercher(filtre, zoneId, categorieId,
                        StatutVerification.VALIDE, StatutCompte.ACTIF)
                .stream()
                .filter(pro -> pro.getLatitude() != null && pro.getLongitude() != null)
                .map(pro -> new ProfessionnelProcheResponse(
                        mapper.toResume(pro, avis.countByProfessionnelId(pro.getId())),
                        pro.getLatitude(), pro.getLongitude(),
                        distanceKm(latitude, longitude, pro.getLatitude(), pro.getLongitude())))
                .filter(pro -> pro.distanceKm() <= rayonKm)
                .sorted(Comparator.comparingDouble(ProfessionnelProcheResponse::distanceKm))
                .toList();
    }

    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double a = Math.pow(Math.sin(Math.toRadians(lat2 - lat1) / 2), 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(Math.toRadians(lon2 - lon1) / 2), 2);
        return 6371.0088 * 2 * Math.asin(Math.sqrt(Math.min(1, Math.max(0, a))));
    }

    private void verifier(double latitude, double longitude, double rayonKm) {
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)
                || !Double.isFinite(rayonKm) || latitude < -90 || latitude > 90
                || longitude < -180 || longitude > 180 || rayonKm < 1 || rayonKm > 100) {
            throw new BusinessException("Coordonnées invalides ou rayon hors de 1 à 100 km");
        }
    }

    private Utilisateur professionnel(Long id) {
        return utilisateurs.findByIdAndRoleNom(id, NomRole.PROFESSIONNEL)
                .orElseThrow(() -> new ResourceNotFoundException("Professionnel introuvable"));
    }
}
