package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Un SMS "envoyé" en mode simulation (aucun vrai SMS ne part).
// La date d'envoi est createdAt (dans BaseEntity).
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sms_simules")
public class SmsSimule extends BaseEntity {

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(nullable = false, length = 500)
    private String contenu;
}