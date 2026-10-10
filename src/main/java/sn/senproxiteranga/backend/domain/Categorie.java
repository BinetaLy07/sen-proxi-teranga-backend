package sn.senproxiteranga.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.senproxiteranga.backend.domain.enums.FamilleCategorie;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "categories")
public class Categorie extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    // Une petite image (emoji) affichée devant le nom, ex. "🔧". Facultative.
    @Column(length = 16)
    private String icone;

    // La famille où ranger la catégorie dans « Trouver un pro ». Facultative :
    // sans famille, la catégorie s'affiche dans « Autres services ».
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FamilleCategorie famille;

    public void activer() {
        this.active = true;
    }

    public void desactiver() {
        this.active = false;
    }
}
