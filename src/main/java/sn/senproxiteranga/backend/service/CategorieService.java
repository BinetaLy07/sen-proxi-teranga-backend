package sn.senproxiteranga.backend.service;

import sn.senproxiteranga.backend.dto.CategorieRequest;
import sn.senproxiteranga.backend.dto.CategorieResponse;

import java.util.List;

public interface CategorieService {

    CategorieResponse creer(CategorieRequest request);

    CategorieResponse modifier(Long id, CategorieRequest request);

    CategorieResponse activer(Long id);

    CategorieResponse desactiver(Long id);

    List<CategorieResponse> listerToutes();

    List<CategorieResponse> listerActives();

    CategorieResponse trouverParId(Long id);
}