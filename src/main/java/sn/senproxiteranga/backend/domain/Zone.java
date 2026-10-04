package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.senproxiteranga.backend.domain.enums.TypeZone;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "zones")
public class Zone extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeZone type;

    // Position sur la carte (facultative), pour le tri par proximité
    private Double latitude;

    private Double longitude;

    // Pour une COMMUNE : la région à laquelle elle appartient
    // (vide pour une REGION et pour un QUARTIER)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Zone region;

    // Pour un QUARTIER : la commune à laquelle il appartient
    // (vide pour une REGION et pour une COMMUNE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_id")
    private Zone commune;
}