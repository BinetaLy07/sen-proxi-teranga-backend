package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.senproxiteranga.backend.domain.enums.StatutVerification;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "professionnels")
@DiscriminatorValue("PROFESSIONNEL")
public class Professionnel extends Utilisateur {

    @Column(nullable = false, length = 100)
    private String metier;

    @Column(columnDefinition = "TEXT")
    private String competences;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20)
    private String whatsapp;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_verification", nullable = false, length = 30)
    private StatutVerification statutVerification = StatutVerification.EN_ATTENTE;

    @Column(name = "alerte_sms_active", nullable = false)
    private boolean alerteSmsActive = false;

    @Column(name = "note_moyenne", nullable = false)
    private double noteMoyenne = 0.0;

    // Zones d'intervention : un professionnel travaille dans plusieurs zones
    @ManyToMany
    @JoinTable(
            name = "professionnel_zones",
            joinColumns = @JoinColumn(name = "professionnel_id"),
            inverseJoinColumns = @JoinColumn(name = "zone_id")
    )
    private Set<Zone> zones = new HashSet<>();
}