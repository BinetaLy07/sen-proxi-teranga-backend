-- À exécuter sur la base existante avant de redémarrer l'application.
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(20) NOT NULL UNIQUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6)
);
INSERT IGNORE INTO roles (nom, created_at) VALUES
    ('CLIENT', UTC_TIMESTAMP(6)),
    ('PROFESSIONNEL', UTC_TIMESTAMP(6)),
    ('ADMINISTRATEUR', UTC_TIMESTAMP(6));
ALTER TABLE utilisateurs ADD COLUMN role_id BIGINT NULL;
UPDATE utilisateurs u JOIN roles r ON r.nom = u.role SET u.role_id = r.id;
ALTER TABLE utilisateurs MODIFY COLUMN role_id BIGINT NOT NULL;
ALTER TABLE utilisateurs ADD CONSTRAINT fk_utilisateur_role
    FOREIGN KEY (role_id) REFERENCES roles(id);
