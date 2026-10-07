package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.senproxiteranga.backend.domain.enums.NomRole;
import sn.senproxiteranga.backend.domain.enums.StatutRendezVous;

import java.time.LocalDateTime;

/**
 * Une date proposée pour réaliser les travaux d'une demande.
 *
 * Règle : c'est le CLIENT qui choisit la première date (le jour et l'heure).
 * Celui qui propose attend ; c'est l'AUTRE qui répond :
 * - il accepte  -> la demande passe à PLANIFIEE ;
 * - ou il propose une autre date -> la date précédente passe à REFUSE.
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

    // Qui a proposé cette date : CLIENT ou PROFESSIONNEL (c'est l'autre qui doit répondre).
    // Vide pour les anciens rendez-vous, créés quand seul le professionnel proposait.
    @Enumerated(EnumType.STRING)
    @Column(name = "propose_par", length = 20)
    private NomRole proposePar;

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

    /**
     * Qui a proposé cette date.
     * On écrit ce getter nous-mêmes (Lombok n'en génère pas un 2e) :
     * pour un ancien rendez-vous sans auteur, c'était forcément le professionnel.
     */
    public NomRole getProposePar() {
        return proposePar != null ? proposePar : NomRole.PROFESSIONNEL;
    }
}
