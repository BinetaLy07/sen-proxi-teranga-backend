package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * L'avis laissé par un client sur le travail d'un professionnel, après une demande.
 * Un seul avis par demande, définitif une fois publié.
 * Le professionnel peut y répondre une seule fois.
 */
@Entity
@Table(name = "avis")
@Getter
@Setter
@NoArgsConstructor
public class Avis extends BaseEntity {

    // La demande concernée (un seul avis par demande : unique = true)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_id", nullable = false, unique = true)
    private Demande demande;

    // Le professionnel noté (copié de la demande pour retrouver vite tous ses avis)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professionnel_id", nullable = false)
    private Professionnel professionnel;

    // Note de 1 à 5 étoiles
    @Column(nullable = false)
    private int note;

    // Commentaire du client (facultatif)
    @Column(length = 1000)
    private String commentaire;

    // Réponse du professionnel (facultative, une seule fois)
    @Column(length = 1000)
    private String reponse;

    @Column(name = "date_reponse")
    private LocalDateTime dateReponse;
}