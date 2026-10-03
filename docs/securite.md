# Sécurité de l’API

Chaque utilisateur possède un rôle via `utilisateurs.role_id` vers `roles.id` : CLIENT, PROFESSIONNEL ou ADMINISTRATEUR. Les rôles sont initialisés au démarrage. L’inscription attribue le rôle côté serveur ; le client ne peut pas choisir un rôle administrateur.

Une seule entité concrète `Utilisateur` contient les champs communs, l’adresse et la zone du client, les informations et les zones d’intervention du professionnel, ainsi que la date du dernier accès administrateur. Les champs spécifiques sont facultatifs en base. Le formulaire d’inscription professionnel conserve ses validations et initialise le statut de vérification à EN_ATTENTE. Le rôle est la seule distinction entre les comptes. Les anciennes classes, leurs repositories, l’héritage JOINED et le discriminateur JPA sont supprimés.

Pour une base utilisant encore les anciennes tables, arrêter l’application et sauvegarder la base, puis exécuter une seule fois :

1. `docs/security-role-migration.sql` si la relation vers roles n’existe pas encore.
2. `docs/security-user-migration.sql` pour copier les profils dans utilisateurs, conserver les IDs, rediriger les clés étrangères et supprimer les anciennes tables et la colonne discriminatrice.

Ces scripts ne sont pas exécutés automatiquement. Hibernate `ddl-auto: update` ne suffit pas pour copier les données ou déplacer les clés étrangères. Sur une base vierge, Hibernate crée directement le nouveau modèle. Les scripts concernent le schéma MySQL original du projet ; ils doivent être vérifiés sur une copie de la base existante avant utilisation.

## Inscription publique

`POST /api/auth/register` est accessible sans token et retourne HTTP 201 avec l’utilisateur créé (sans mot de passe). Exemple client :

```json
{
  "prenom": "Awa",
  "nom": "Diop",
  "telephone": "771234567",
  "email": "awa@example.com",
  "motDePasse": "MotDePasse123!",
  "cguAcceptees": true,
  "role": "CLIENT"
}
```

Pour un professionnel, envoyer `role: "PROFESSIONNEL"` et `metier`, puis éventuellement `competences`, `description`, `whatsapp` et `zoneIds`. Le rôle ADMINISTRATEUR est refusé dans ce formulaire et dans le service. Les anciens endpoints d’inscription restent accessibles pour compatibilité.

## Authentification

- POST /api/auth/connexion : `{ "email": "client@example.com", "motDePasse": "..." }`.
- POST /api/auth/refresh : `{ "refreshToken": "..." }`.
- POST /api/auth/deconnexion : révoque la session courante.
- POST /api/auth/deconnexion-toutes : révoque toutes les sessions du compte.
- GET /api/auth/sessions : liste les sessions actives du compte.
- DELETE /api/auth/sessions/{id} : révoque une session appartenant au compte.

Envoyer `Authorization: Bearer <accessToken>` sur les endpoints protégés. Les tokens sont opaques, générés avec 256 bits aléatoires ; seuls leurs condensats SHA-256 sont stockés. Les access tokens expirent après 15 minutes et les sessions après 7 jours. Configurer `security.access-ttl` et `security.refresh-ttl` avec des durées ISO-8601. Le renouvellement remplace les deux tokens et invalide les anciens, sans prolonger la durée totale de la session. Le verrou pessimiste empêche deux renouvellements simultanés du même token.

Les sessions sont persistées en base, sans HttpSession/JSESSIONID. La révocation et la suspension d’un compte prennent effet dès la prochaine requête. Les tokens sont transmis explicitement dans l’en-tête ou le corps, jamais lus depuis des cookies. Utiliser HTTPS et un stockage approprié côté client.

## Autorisations

Le catalogue (catégories, zones et services) est consultable publiquement. La gestion des catégories et des zones et la liste de toutes les catégories nécessitent ADMINISTRATEUR. Les URLs clients et professionnels vérifient le rôle et l’identifiant du compte connecté. Les demandes et les devis ne sont consultables que par leur client, leur professionnel ou un administrateur. Les endpoints non déclarés sont refusés par défaut. Réponses 401 sans authentification valide, 403 lorsque l’accès est interdit.

L’API conserve les IDs dans les URLs pour compatibilité mais vérifie leur propriété. Aucun endpoint public de création ou promotion d’administrateur n’est exposé.

## Création par un administrateur

`POST /api/admin/utilisateurs` exige un access token valide et le rôle ADMINISTRATEUR, vérifié dans la configuration HTTP. La sécurité des méthodes est activée avec `@EnableMethodSecurity`. Cet endpoint retourne HTTP 201 et utilise les mêmes champs que register, mais accepte également le rôle ADMINISTRATEUR. Le mot de passe est haché et n’est jamais retourné. Les autres contrôleurs n’ont pas reçu d’annotations supplémentaires.

Exemple pour créer un administrateur :

```json
{
  "prenom": "Awa",
  "nom": "Diop",
  "telephone": "771234567",
  "email": "admin@example.com",
  "motDePasse": "MotDePasse123!",
  "cguAcceptees": true,
  "role": "ADMINISTRATEUR"
}
```

Cet endpoint nécessite un administrateur connecté. Le premier compte est initialisé au démarrage comme décrit ci-dessous.

## Statut des comptes

Les nouveaux comptes sont ACTIF par défaut ; aucun statut n’est accepté dans le formulaire de création.

`PATCH /api/admin/utilisateurs/{id}/statut-compte` est protégé par `@PreAuthorize("hasRole('ADMINISTRATEUR')")`. Envoyer `{ "statutCompte": "SUSPENDU" }` pour suspendre ou `{ "statutCompte": "ACTIF" }` pour réactiver. La suspension révoque les sessions existantes. Après réactivation, l’utilisateur doit se reconnecter ; ses anciens tokens restent révoqués.

## Administrateur initial au démarrage

À chaque démarrage, l’application crée les rôles manquants, puis recherche l’administrateur initial par email. Si le compte est présent et possède le rôle ADMINISTRATEUR, il reste inchangé (mot de passe, statut et informations). S’il est absent, il est créé avec le rôle ADMINISTRATEUR et le statut ACTIF. Un compte supprimé sera donc recréé au prochain démarrage avec la configuration disponible. La suppression d’un rôle encore utilisé est normalement empêchée par la clé étrangère.

Configuration par variables d’environnement :

- `ADMIN_EMAIL` : `admin@senproxiteranga.sn` par défaut, à garder stable pour reconnaître le même compte.
- `ADMIN_TELEPHONE` : `770000000` par défaut ; il doit être libre pour une création.
- `ADMIN_PRENOM` et `ADMIN_NOM` : `Admin` et `Teranga` par défaut.
- `ADMIN_PASSWORD` : aucun mot de passe par défaut ; obligatoire lorsque le compte n’existe pas. BCrypt est utilisé pour le hachage.

Exemple PowerShell pour lancer le backend :

```powershell
$env:ADMIN_PASSWORD = 'RemplacerParUnMotDePasseFort!'
.\mvnw.cmd spring-boot:run
```

Le démarrage échoue avec un message de configuration si le compte doit être créé sans mot de passe valide, si l’email appartient à un compte non administrateur ou si le téléphone est déjà utilisé. Aucun compte existant n’est promu ni réinitialisé. Pour permettre la recréation après suppression, conserver la configuration du mot de passe dans l’environnement de lancement. Le mot de passe n’est pas journalisé. La création technique ne déclare pas de fausse acceptation des CGU.
