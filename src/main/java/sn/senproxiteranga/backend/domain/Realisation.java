package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Une photo d'un ancien travail du professionnel, affichée sur son profil.
 * Facultatif : un professionnel peut ne mettre aucune réalisation.
 *
 * Le fichier est rangé sur le disque (dossier "uploads"),
 * la base ne garde que ses informations.
 */
@Entity
@Table(name = "realisations")
@Getter
@Setter
@NoArgsConstructor
public class Realisation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professionnel_id", nullable = false)
    private Utilisateur professionnel;

    // Ex : "Salle de bain carrelée à Mermoz"
    @Column(nullable = false, length = 150)
    private String titre;

    // Facultative : quelques détails sur le travail réalisé
    @Column(length = 500)
    private String description;

    // Nom du fichier tel que le pro l'a envoyé
    @Column(name = "nom_original", nullable = false, length = 255)
    private String nomOriginal;

    // Nom unique sur le disque (généré par le serveur)
    @Column(name = "nom_stocke", nullable = false, unique = true, length = 100)
    private String nomStocke;

    // "image/jpeg" ou "image/png"
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;
}
