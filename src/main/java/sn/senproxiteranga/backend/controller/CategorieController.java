package sn.senproxiteranga.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.senproxiteranga.backend.dto.CategorieRequest;
import sn.senproxiteranga.backend.dto.CategorieResponse;
import sn.senproxiteranga.backend.service.CategorieService;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategorieController {

    private final CategorieService categorieService;

    // Catalogue public : catégories actives
    @GetMapping
    public List<CategorieResponse> listerActives() {
        return categorieService.listerActives();
    }

    // Administrateur : toutes les catégories
    @GetMapping("/toutes")
    public List<CategorieResponse> listerToutes() {
        return categorieService.listerToutes();
    }

    @GetMapping("/{id}")
    public CategorieResponse trouverParId(@PathVariable Long id) {
        return categorieService.trouverParId(id);
    }

    @PostMapping
    public ResponseEntity<CategorieResponse> creer(@Valid @RequestBody CategorieRequest request) {
        CategorieResponse creee = categorieService.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creee);
    }

    @PutMapping("/{id}")
    public CategorieResponse modifier(@PathVariable Long id,
                                      @Valid @RequestBody CategorieRequest request) {
        return categorieService.modifier(id, request);
    }

    @PatchMapping("/{id}/activer")
    public CategorieResponse activer(@PathVariable Long id) {
        return categorieService.activer(id);
    }

    @PatchMapping("/{id}/desactiver")
    public CategorieResponse desactiver(@PathVariable Long id) {
        return categorieService.desactiver(id);
    }
}