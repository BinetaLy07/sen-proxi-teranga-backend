package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Un professionnel ajouté aux favoris d'un client.
 * La date d'ajout est le createdAt de BaseEntity.
 */
@Entity
@Table(
        name = "favoris",
        // Un même professionnel ne peut être qu'une seule fois dans les favoris d'un client
        uniqueConstraints = @UniqueConstraint(
                name = "uk_favori_client_professionnel",
                columnNames = {"client_id", "professionnel_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Favori extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Utilisateur client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professionnel_id", nullable = false)
    private Utilisateur professionnel;
}
