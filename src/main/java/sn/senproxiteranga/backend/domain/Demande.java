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

    // ----- Liens -----

    // Le client qui a créé la demande
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    // Le professionnel qui doit la traiter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professionnel_id", nullable = false)
    private Professionnel professionnel;

    // Le service demandé
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceProfessionnel service;

    // Le quartier ou la commune de l'intervention (facultatif)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;
}