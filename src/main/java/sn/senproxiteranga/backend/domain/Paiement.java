package sn.senproxiteranga.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.senproxiteranga.backend.domain.enums.ModePaiement;
import sn.senproxiteranga.backend.domain.enums.StatutPaiement;

import java.time.LocalDateTime;

/**
 * Le paiement d'une demande, déclaré par le client puis confirmé (ou contesté) par le professionnel.
 * Une demande n'a qu'un seul paiement.
 */
@Entity
@Table(name = "paiements")
@Getter
@Setter
@NoArgsConstructor
public class Paiement extends BaseEntity {

    // La demande payée (une seule fois : unique = true)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_id", nullable = false, unique = true)
    private Demande demande;

    // Montant total du devis accepté (calculé par le serveur, jamais saisi par le client)
    @Column(nullable = false)
    private Double montant;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement", nullable = false, length = 20)
    private ModePaiement modePaiement;

    // Numéro de transaction Wave / Orange Money / Free Money (facultatif, vide pour les espèces)
    @Column(length = 100)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutPaiement statut = StatutPaiement.DECLARE;

    // Le professionnel doit confirmer avant cette date (72 h après la déclaration)
    @Column(name = "date_limite_confirmation", nullable = false)
    private LocalDateTime dateLimiteConfirmation;

    // Remplie quand le professionnel confirme ou conteste
    @Column(name = "date_reponse")
    private LocalDateTime dateReponse;

    // Raison de la contestation (ex : "Je n'ai rien reçu sur mon compte Wave")
    @Column(name = "motif_contestation", length = 500)
    private String motifContestation;
}