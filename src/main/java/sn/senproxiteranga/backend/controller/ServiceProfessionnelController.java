package sn.senproxiteranga.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.senproxiteranga.backend.dto.ServiceRequest;
import sn.senproxiteranga.backend.dto.ServiceResponse;
import sn.senproxiteranga.backend.service.ServiceProfessionnelService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ServiceProfessionnelController {

    private final ServiceProfessionnelService serviceProService;

    // Services d'un professionnel (actifs par défaut)
    @GetMapping("/professionnels/{proId}/services")
    public List<ServiceResponse> lister(@PathVariable Long proId,
                                        @RequestParam(defaultValue = "true") boolean actifsSeulement) {
        return serviceProService.listerParProfessionnel(proId, actifsSeulement);
    }

    // Détail d'un service
    @GetMapping("/services/{serviceId}")
    public ServiceResponse trouverParId(@PathVariable Long serviceId) {
        return serviceProService.trouverParId(serviceId);
    }

    @PostMapping("/professionnels/{proId}/services")
    public ResponseEntity<ServiceResponse> creer(@PathVariable Long proId,
                                                 @Valid @RequestBody ServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceProService.creer(proId, request));
    }

    @PutMapping("/professionnels/{proId}/services/{serviceId}")
    public ServiceResponse modifier(@PathVariable Long proId,
                                    @PathVariable Long serviceId,
                                    @Valid @RequestBody ServiceRequest request) {
        return serviceProService.modifier(proId, serviceId, request);
    }

    @PatchMapping("/professionnels/{proId}/services/{serviceId}/activer")
    public ServiceResponse activer(@PathVariable Long proId, @PathVariable Long serviceId) {
        return serviceProService.activer(proId, serviceId);
    }

    @PatchMapping("/professionnels/{proId}/services/{serviceId}/desactiver")
    public ServiceResponse desactiver(@PathVariable Long proId, @PathVariable Long serviceId) {
        return serviceProService.desactiver(proId, serviceId);
    }

    @DeleteMapping("/professionnels/{proId}/services/{serviceId}")
    public ResponseEntity<Void> supprimer(@PathVariable Long proId, @PathVariable Long serviceId) {
        serviceProService.supprimer(proId, serviceId);
        return ResponseEntity.noContent().build();
    }
}