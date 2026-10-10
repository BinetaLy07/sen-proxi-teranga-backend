package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import sn.senproxiteranga.backend.domain.enums.NomRole;
import sn.senproxiteranga.backend.domain.enums.StatutCompte;
import sn.senproxiteranga.backend.domain.enums.StatutVerification;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "utilisateurs")
public class Utilisateur extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, unique = true, length = 20)
    private String telephone;

    // Facultatif (vide pour ceux qui n'ont pas d'email). S'il existe, il est unique.
    @Column(unique = true, length = 150)
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

    @Column(length = 255)
    private String adresse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @Column(length = 100)
    private String metier;

    @Column(columnDefinition = "TEXT")
    private String competences;

    @Column
    private Integer experience;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20)
    private String whatsapp;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_verification", length = 30)
    private StatutVerification statutVerification;

    // Rempli par l'administrateur quand il demande une correction ou refuse le profil
    // (vidé quand le profil est validé)
    @Column(name = "motif_verification", length = 500)
    private String motifVerification;

    @Column(name = "alerte_sms_active", nullable = false)
    private boolean alerteSmsActive;

    @Column(name = "note_moyenne", nullable = false)
    private double noteMoyenne;

    @ManyToMany
    @JoinTable(
            name = "utilisateur_zones",
            joinColumns = @JoinColumn(name = "utilisateur_id"),
            inverseJoinColumns = @JoinColumn(name = "zone_id"))
    private Set<Zone> zones = new HashSet<>();

    // Dernière connexion. Vide = l'utilisateur ne s'est encore jamais connecté
    // (sert à afficher la fenêtre de bienvenue une seule fois, juste après l'inscription)
    @Column(name = "dernier_acces")
    private LocalDateTime dernierAcces;

    public boolean aRole(NomRole nomRole) {
        return role != null && role.getNom() == nomRole;
    }
}