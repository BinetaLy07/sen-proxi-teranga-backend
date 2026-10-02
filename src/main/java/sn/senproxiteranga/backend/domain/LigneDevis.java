package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.senproxiteranga.backend.domain.enums.TypeLigneDevis;

/**
 * Une ligne d'un devis.
 * Exemple : MATERIEL | Joints | quantité 2 | 250 F | montant 500 F
 */
@Entity
@Table(name = "lignes_devis")
@Getter
@Setter
@NoArgsConstructor
public class LigneDevis extends BaseEntity {

    // Le devis auquel appartient cette ligne
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "devis_id", nullable = false)
    private Devis devis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeLigneDevis type;

    @Column(nullable = false, length = 150)
    private String libelle;

    @Column(nullable = false)
    private int quantite = 1;

    @Column(name = "prix_unitaire", nullable = false)
    private Double prixUnitaire;

    // Calculé automatiquement : quantité × prix unitaire
    @Column(nullable = false)
    private Double montant = 0.0;

    /**
     * Montant de la ligne = quantité × prix unitaire.
     */
    public void calculerMontant() {
        this.montant = quantite * prixUnitaire;
    }
}