package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "utilisateurs")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class Utilisateur extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, unique = true, length = 20)
    private String telephone;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "mot_de_passe_hache", nullable = false)
    private String motDePasseHache;

    private String photo;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_compte", nullable = false, length = 20)
    private StatutCompte statutCompte = StatutCompte.ACTIF;

    @Column(name = "cgu_acceptees", nullable = false)
    private boolean cguAcceptees;

    @Column(name = "date_acceptation_cgu")
    private LocalDateTime dateAcceptationCgu;
}