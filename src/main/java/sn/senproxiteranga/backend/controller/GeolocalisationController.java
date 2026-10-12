package sn.senproxiteranga.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.senproxiteranga.backend.dto.*;
import sn.senproxiteranga.backend.service.GeolocalisationService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GeolocalisationController {

    private final GeolocalisationService geolocalisation;

    // EndpointAccess réserve ces routes au professionnel concerné ou à l'administrateur.
    @GetMapping("/professionnels/{proId}/position")
    public PositionResponse position(@PathVariable Long proId) {
        return geolocalisation.position(proId);
    }

    @PutMapping("/professionnels/{proId}/position")
    public PositionResponse enregistrer(@PathVariable Long proId,
                                       @Valid @RequestBody PositionRequest position) {
        return geolocalisation.enregistrer(proId, position);
    }

    @DeleteMapping("/professionnels/{proId}/position")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long proId) {
        geolocalisation.supprimer(proId);
    }

    @GetMapping("/proximite/professionnels")
    public List<ProfessionnelProcheResponse> rechercher(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "10") double rayonKm,
            @RequestParam(required = false) String metier,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long categorieId) {
        return geolocalisation.rechercher(latitude, longitude, rayonKm, metier, zoneId, categorieId);
    }
}
