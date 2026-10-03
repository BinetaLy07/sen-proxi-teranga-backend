package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import sn.senproxiteranga.backend.domain.enums.TypeTarif;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "services")
public class ServiceProfessionnel extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_tarif", nullable = false, length = 20)
    private TypeTarif typeTarif;

    // Vide si le tarif est SUR_DEVIS
    private Double montant;

    @Column(nullable = false)
    private boolean actif = true;

    // Le professionnel qui propose ce service
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professionnel_id", nullable = false)
    private Utilisateur professionnel;

    // La catégorie du service (ex : Plomberie)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    public void activer() {
        this.actif = true;
    }

    public void desactiver() {
        this.actif = false;
    }
}
