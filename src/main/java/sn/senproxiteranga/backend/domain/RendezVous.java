package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.senproxiteranga.backend.domain.enums.StatutRendezVous;

import java.time.LocalDateTime;

/**
 * Un rendez-vous proposé par le professionnel pour réaliser les travaux d'une demande.
 *
 * Une demande peut avoir plusieurs rendez-vous : les dates refusées ou reportées
 * sont gardées (historique), un seul est "actif" à la fois (PROPOSE ou ACCEPTE).
 */
@Entity
@Table(name = "rendez_vous")
@Getter
@Setter
@NoArgsConstructor
public class RendezVous extends BaseEntity {

    // La demande concernée
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_id", nullable = false)
    private Demande demande;

    // Date et heure proposées pour les travaux (ex : samedi 10 octobre à 9 h)
    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutRendezVous statut = StatutRendezVous.PROPOSE;

    // Raison d'un refus ou d'un report (ex : "Je ne suis pas là samedi")
    @Column(length = 500)
    private String motif;

    // Remplies quand le professionnel commence puis termine le travail
    @Column(name = "date_debut_travaux")
    private LocalDateTime dateDebutTravaux;

    @Column(name = "date_fin_travaux")
    private LocalDateTime dateFinTravaux;
}