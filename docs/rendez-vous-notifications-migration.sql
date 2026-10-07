-- À exécuter UNE fois sur la base existante (phpMyAdmin > sen_proxi_teranga > onglet SQL).
--
-- Pourquoi ? Hibernate a créé la colonne "type" comme une liste fermée (ENUM)
-- avec les types de notification qui existaient ce jour-là.
-- "ddl-auto: update" ajoute les nouvelles colonnes, mais ne modifie jamais
-- une colonne existante : les nouveaux types (RENDEZ_VOUS_PROPOSE,
-- RENDEZ_VOUS_ACCEPTE, RENDEZ_VOUS_REPORTE) seraient refusés par MariaDB
-- ("Data truncated for column 'type'").
-- En VARCHAR, la colonne accepte n'importe quel type de notification.
ALTER TABLE notifications MODIFY COLUMN type VARCHAR(30) NOT NULL;
