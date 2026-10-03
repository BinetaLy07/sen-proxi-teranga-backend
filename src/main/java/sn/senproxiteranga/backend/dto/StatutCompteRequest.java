package sn.senproxiteranga.backend.dto;

import jakarta.validation.constraints.NotNull;

import sn.senproxiteranga.backend.domain.enums.StatutCompte;

public record StatutCompteRequest(@NotNull StatutCompte statutCompte) {}
