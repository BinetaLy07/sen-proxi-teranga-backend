package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.senproxiteranga.backend.domain.Message;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // ---------- Questions générales (messages SANS demande) ----------

    // Les questions générales échangées entre 2 utilisateurs, dans les 2 sens,
    // du plus ancien au plus récent (comme dans WhatsApp)
    @Query("""
            SELECT m FROM Message m
            WHERE m.demande IS NULL
              AND ((m.expediteur.id = :a AND m.destinataire.id = :b)
                OR (m.expediteur.id = :b AND m.destinataire.id = :a))
            ORDER BY m.createdAt ASC, m.id ASC
            """)
    List<Message> conversationGenerale(@Param("a") Long utilisateurA, @Param("b") Long utilisateurB);

    // Les questions générales de "expediteur" à "destinataire" pas encore lues
    List<Message> findByExpediteurIdAndDestinataireIdAndDemandeIsNullAndLuFalse(
            Long expediteurId, Long destinataireId);

    // ---------- Discussion d'une demande ----------

    // Tous les messages d'une demande, du plus ancien au plus récent
    List<Message> findByDemandeIdOrderByCreatedAtAscIdAsc(Long demandeId);

    // Les messages d'une demande reçus par "destinataire" et pas encore lus
    List<Message> findByDemandeIdAndDestinataireIdAndLuFalse(Long demandeId, Long destinataireId);

    // ---------- Pour tout le monde ----------

    // Tous les messages envoyés OU reçus par un utilisateur, les plus récents d'abord
    // (sert à construire la liste de ses conversations)
    @Query("""
            SELECT m FROM Message m
            WHERE m.expediteur.id = :id OR m.destinataire.id = :id
            ORDER BY m.createdAt DESC, m.id DESC
            """)
    List<Message> tousLesMessagesDe(@Param("id") Long utilisateurId);

    // Nombre total de messages non lus reçus par un utilisateur (pour la pastille rouge)
    long countByDestinataireIdAndLuFalse(Long destinataireId);

    // "expediteur" a-t-il déjà écrit à "destinataire" ? (règle 5)
    boolean existsByExpediteurIdAndDestinataireId(Long expediteurId, Long destinataireId);
}
