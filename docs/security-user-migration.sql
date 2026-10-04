-- Migration unique du modèle JOINED vers une seule table utilisateurs.
-- Arrêter l'application et sauvegarder la base avant exécution.
-- Prérequis : security-role-migration.sql a déjà été exécuté.
-- Les DDL MySQL ne sont pas annulables par un simple ROLLBACK.
ALTER TABLE utilisateurs
    ADD COLUMN adresse VARCHAR(255),
    ADD COLUMN zone_id BIGINT,
    ADD COLUMN metier VARCHAR(100),
    ADD COLUMN competences TEXT,
    ADD COLUMN experience INT,
    ADD COLUMN description TEXT,
    ADD COLUMN whatsapp VARCHAR(20),
    ADD COLUMN statut_verification VARCHAR(30),
    ADD COLUMN alerte_sms_active BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN note_moyenne DOUBLE NOT NULL DEFAULT 0,
    ADD COLUMN dernier_acces DATETIME(6);

UPDATE utilisateurs u JOIN clients c ON c.id = u.id
SET u.adresse = c.adresse, u.zone_id = c.zone_id;

UPDATE utilisateurs u JOIN professionnels p ON p.id = u.id
SET u.metier = p.metier,
    u.competences = p.competences,
    u.description = p.description,
    u.whatsapp = p.whatsapp,
    u.statut_verification = p.statut_verification,
    u.alerte_sms_active = p.alerte_sms_active,
    u.note_moyenne = p.note_moyenne;

-- La colonne experience existe dans les versions récentes du module profil.
-- La copier uniquement lorsqu'elle est présente dans la table d'origine.
SET @migration_experience = IF(
    EXISTS(SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'professionnels'
             AND COLUMN_NAME = 'experience'),
    'UPDATE utilisateurs u JOIN professionnels p ON p.id = u.id SET u.experience = p.experience',
    'SELECT 1');
PREPARE experience_copy FROM @migration_experience;
EXECUTE experience_copy;
DEALLOCATE PREPARE experience_copy;

UPDATE utilisateurs u JOIN administrateurs a ON a.id = u.id
SET u.dernier_acces = a.dernier_acces;

ALTER TABLE utilisateurs ADD CONSTRAINT fk_utilisateur_zone
    FOREIGN KEY (zone_id) REFERENCES zones(id);

-- Rediriger les clés étrangères vers utilisateurs en conservant les IDs.
-- Les associations du modèle existant utilisent des clés simples.
DELIMITER $$
CREATE PROCEDURE migrate_user_foreign_keys()
BEGIN
    DECLARE finished BOOLEAN DEFAULT FALSE;
    DECLARE target_table VARCHAR(64);
    DECLARE target_constraint VARCHAR(64);
    DECLARE target_column VARCHAR(64);
    DECLARE fk_cursor CURSOR FOR
        SELECT TABLE_NAME, CONSTRAINT_NAME, COLUMN_NAME
        FROM information_schema.KEY_COLUMN_USAGE
        WHERE TABLE_SCHEMA = DATABASE()
          AND REFERENCED_TABLE_NAME IN ('clients', 'professionnels', 'administrateurs');
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = TRUE;

    OPEN fk_cursor;
    fk_loop: LOOP
        FETCH fk_cursor INTO target_table, target_constraint, target_column;
        IF finished THEN
            LEAVE fk_loop;
        END IF;
        SET @migration_sql = CONCAT('ALTER TABLE `', target_table,
            '` DROP FOREIGN KEY `', target_constraint, '`');
        PREPARE statement_handle FROM @migration_sql;
        EXECUTE statement_handle;
        DEALLOCATE PREPARE statement_handle;
        SET @migration_sql = CONCAT('ALTER TABLE `', target_table,
            '` ADD CONSTRAINT `', target_constraint, '` FOREIGN KEY (`',
            target_column, '`) REFERENCES utilisateurs(id)');
        PREPARE statement_handle FROM @migration_sql;
        EXECUTE statement_handle;
        DEALLOCATE PREPARE statement_handle;
    END LOOP;
    CLOSE fk_cursor;
END$$
DELIMITER ;
CALL migrate_user_foreign_keys();
DROP PROCEDURE migrate_user_foreign_keys;

RENAME TABLE professionnel_zones TO utilisateur_zones;
ALTER TABLE utilisateur_zones CHANGE COLUMN professionnel_id utilisateur_id BIGINT NOT NULL;

-- Supprimer les anciennes tables après copie et redirection des relations.
DROP TABLE clients;
DROP TABLE professionnels;
DROP TABLE administrateurs;
ALTER TABLE utilisateurs DROP COLUMN role;
