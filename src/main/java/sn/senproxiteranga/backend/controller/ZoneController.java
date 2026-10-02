package sn.senproxiteranga.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.senproxiteranga.backend.domain.enums.TypeZone;
import sn.senproxiteranga.backend.dto.ZoneRequest;
import sn.senproxiteranga.backend.dto.ZoneResponse;
import sn.senproxiteranga.backend.service.ZoneService;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneService zoneService;

    // /api/zones  ou  /api/zones?type=COMMUNE  ou  /api/zones?type=QUARTIER
    @GetMapping
    public List<ZoneResponse> lister(@RequestParam(required = false) TypeZone type) {
        return zoneService.lister(type);
    }

    @GetMapping("/{id}")
    public ZoneResponse trouverParId(@PathVariable Long id) {
        return zoneService.trouverParId(id);
    }

    // Les quartiers d'une commune : /api/zones/1/quartiers
    @GetMapping("/{id}/quartiers")
    public List<ZoneResponse> listerQuartiers(@PathVariable Long id) {
        return zoneService.listerQuartiers(id);
    }

    @PostMapping
    public ResponseEntity<ZoneResponse> creer(@Valid @RequestBody ZoneRequest request) {
        ZoneResponse creee = zoneService.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creee);
    }

    @PutMapping("/{id}")
    public ZoneResponse modifier(@PathVariable Long id,
                                 @Valid @RequestBody ZoneRequest request) {
        return zoneService.modifier(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        zoneService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}