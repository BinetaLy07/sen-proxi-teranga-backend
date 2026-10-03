package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.senproxiteranga.backend.domain.enums.TypeMedia;

/**
 * Une photo ou une vidéo jointe à une demande par le client.
 *
 * Le fichier lui-même est rangé sur le disque (dossier "uploads").
 * La base de données ne garde que ses informations : nom, type, taille...
 */
@Entity
@Table(name = "medias_demandes")
@Getter
@Setter
@NoArgsConstructor
public class MediaDemande extends BaseEntity {

    // La demande à laquelle ce média est rattaché
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_id", nullable = false)
    private Demande demande;

    // IMAGE ou VIDEO
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TypeMedia type;

    // Nom du fichier tel que le client l'a envoyé (ex : "fuite-cuisine.jpg")
    @Column(name = "nom_original", nullable = false, length = 255)
    private String nomOriginal;

    // Nom unique sous lequel le fichier est rangé sur le disque (ex : "a3f9...7b2d.jpg")
    @Column(name = "nom_stocke", nullable = false, unique = true, length = 100)
    private String nomStocke;

    // Format exact du fichier (ex : "image/jpeg", "video/mp4")
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    // Taille du fichier en octets
    @Column(nullable = false)
    private long taille;
}