package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "administrateurs")
@DiscriminatorValue("ADMINISTRATEUR")
public class Administrateur extends Utilisateur {

    @Column(name = "dernier_acces")
    private LocalDateTime dernierAcces;
}