package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.senproxiteranga.backend.domain.enums.StatutDevis;

import java.util.ArrayList;
import java.util.List;

/**
 * Un devis envoyé par un professionnel pour une demande.
 *
 * Une demande peut avoir plusieurs devis : la version 1, puis la version 2
 * si le client demande une révision, puis la version 3 (2 révisions maximum).
 * Les anciennes versions sont gardées avec le statut REMPLACE : c'est l'historique.
 */
@Entity
@Table(name = "devis")
@Getter
@Setter
@NoArgsConstructor
public class Devis extends BaseEntity {

    // La demande à laquelle ce devis répond
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_id", nullable = false)
    private Demande demande;

    // 1 pour le premier devis, 2 après la 1re révision, 3 après la 2e
    @Column(name = "numero_version", nullable = false)
    private int numeroVersion = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutDevis statut = StatutDevis.ENVOYE;

    // Calculé automatiquement à partir des lignes, jamais saisi à la main
    @Column(name = "montant_total", nullable = false)
    private Double montantTotal = 0.0;

    // Rempli quand le client demande une révision (ex : "main-d'œuvre trop chère")
    @Column(name = "motif_revision", length = 500)
    private String motifRevision;

    // Rempli quand le client refuse le devis
    @Column(name = "motif_refus", length = 500)
    private String motifRefus;

    // Les lignes du devis : enregistrées et supprimées en même temps que le devis
    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<LigneDevis> lignes = new ArrayList<>();

    /**
     * Ajoute une ligne au devis : relie la ligne au devis,
     * calcule son montant, puis recalcule le total du devis.
     */
    public void ajouterLigne(LigneDevis ligne) {
        ligne.setDevis(this);
        ligne.calculerMontant();
        lignes.add(ligne);
        calculerTotal();
    }

    /**
     * Total = somme des montants de toutes les lignes.
     */
    public void calculerTotal() {
        this.montantTotal = lignes.stream()
                .mapToDouble(LigneDevis::getMontant)
                .sum();
    }
}