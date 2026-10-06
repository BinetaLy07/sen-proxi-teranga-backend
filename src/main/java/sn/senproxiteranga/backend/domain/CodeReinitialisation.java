package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Un code envoyé par SMS pour réinitialiser un mot de passe oublié.
// La date d'envoi est createdAt (dans BaseEntity).
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "codes_reinitialisation")
public class CodeReinitialisation extends BaseEntity {

    // La personne qui a demandé le code
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    // Le code à 6 chiffres, HACHÉ (jamais en clair dans la base)
    @Column(name = "code_hache", nullable = false, length = 100)
    private String codeHache;

    // Au-delà de cette heure, le code ne marche plus (10 minutes)
    @Column(name = "expire_le", nullable = false)
    private LocalDateTime expireLe;

    // Nombre de codes faux déjà essayés (5 maximum)
    @Column(nullable = false)
    private int tentatives = 0;

    // Un code ne sert qu'une seule fois
    @Column(nullable = false)
    private boolean utilise = false;
}