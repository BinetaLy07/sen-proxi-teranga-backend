package sn.senproxiteranga.backend.security;

public record SessionPrincipal(Long utilisateurId, Long sessionId, String role) {}
