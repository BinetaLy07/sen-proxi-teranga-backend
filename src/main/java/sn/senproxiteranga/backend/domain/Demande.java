package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import sn.senproxiteranga.backend.domain.enums.StatutDemande;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "demandes")
public class Demande extends BaseEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 255)
    private String adresse;

    @Column(name = "date_souhaitee", nullable = false)
    private LocalDate dateSouhaitee;

    @Column(nullable = false)
    private boolean urgente = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutDemande statut = StatutDemande.CREEE;

    // Date limite de réponse du professionnel (ex : 48 h après la création)
    @Column(name = "date_expiration")
    private LocalDateTime dateExpiration;

    @Column(name = "motif_refus", length = 500)
    private String motifRefus;

    @Column(name = "motif_annulation", length = 500)
    private String motifAnnulation;

    // Décision de l'administrateur quand la demande était EN_LITIGE
    // (ex : "Capture Wave vérifiée : transfert bien reçu le 3 octobre")
    @Column(name = "resolution_litige", length = 500)
    private String resolutionLitige;

    // ----- Visite avant devis -----

    // Le client souhaite que le professionnel passe voir avant de faire le devis
    @Column(name = "visite_demandee", nullable = false)
    private boolean visiteDemandee = false;

    // Frais de visite fixés par le professionnel quand il accepte la demande :
    // null = pas de visite demandée, 0 = visite gratuite, > 0 = montant à payer
    @Column(name = "frais_visite")
    private Double fraisVisite;

    // ----- Liens -----

    // Le client qui a créé la demande
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Utilisateur client;

    // Le professionnel qui doit la traiter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professionnel_id", nullable = false)
    private Utilisateur professionnel;

    // Le service demandé
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceProfessionnel service;

    // Le quartier ou la commune de l'intervention (facultatif)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;
}