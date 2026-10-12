package sn.senproxiteranga.backend.dto;

public record ProfessionnelProcheResponse(
        ProfessionnelResumeResponse professionnel,
        double latitude,
        double longitude,
        double distanceKm) {
}
