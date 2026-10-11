# CI/CD sur main

Le workflow `.github/workflows/ci-cd.yml` teste et package l'application avec Java 21 (`mvnw verify`), puis construit une image Docker. Les tests de contexte utilisent H2, pas la base du VPS. Il démarre sur les pushs et pull requests vers `main`, ainsi que manuellement depuis Actions. Les pull requests ne déploient jamais.

Le déploiement des pushs sur `main` est activé seulement lorsque la variable GitHub `DEPLOY_ENABLED` vaut `true`. Une exécution manuelle sur `main` peut aussi déployer. Les déploiements sont sérialisés pour ce dépôt ; un nouveau push n'interrompt pas un déploiement en cours.

## Secrets GitHub

Dans le dépôt : Settings → Secrets and variables → Actions → Secrets.

| Secret | Valeur |
| --- | --- |
| VPS_HOST | `82.29.172.15` |
| VPS_USER | Utilisateur de déploiement ; `root` est compatible, préférer un compte dédié |
| VPS_PORT | Port SSH ; vide signifie `22` |
| VPS_SSH_KEY | Clé privée SSH dédiée au déploiement |
| VPS_KNOWN_HOSTS | Ligne known_hosts correspondant au VPS et à son port SSH |

La clé publique correspondante doit être autorisée sur le VPS dans `~/.ssh/authorized_keys` du compte choisi. Vérifier l'empreinte de la clé hôte via la console de l'hébergeur avant de renseigner `VPS_KNOWN_HOSTS`. Pour un port non standard, utiliser une entrée `[hote]:port`. La vérification SSH n'est jamais désactivée. Le mot de passe root ne sert pas au pipeline et ne doit pas être versionné.

## Préparer le VPS Linux une seule fois

Installer Docker Engine, Bash, gzip et curl. Le compte SSH doit pouvoir utiliser Docker sans saisie interactive. Ces prérequis ne sont pas installés automatiquement, car le système et les applications existantes du VPS ne sont pas encore confirmés.

Comme le VPS ne dispose pas encore de base, le déploiement crée un conteneur MySQL 8.4, la base `sen_proxi_teranga` et l'utilisateur applicatif `senproxi`. Aucun port MySQL n'est publié sur le VPS. Les données restent dans le volume `senproxi-mysql` lors des déploiements suivants.

Après installation de Docker, copier `deploy/init-vps.sh` sur le serveur, puis l'exécuter une seule fois depuis un compte autorisé à écrire dans `/opt` et à utiliser Docker :

```bash
bash init-vps.sh
```

Le script génère des mots de passe aléatoires distincts pour MySQL root, l'utilisateur applicatif et l'administrateur initial. Il crée `/opt/sen-proxi-teranga/mysql.env` et `backend.env`, avec permissions `600`, sans afficher les secrets. Il refuse d'écraser une configuration existante. Le compte SSH du pipeline doit pouvoir lire les deux fichiers. Le mot de passe du compte administrateur de l'application se trouve dans `backend.env`, sous `ADMIN_PASSWORD`.

Au premier déploiement, le pipeline attend que MySQL accepte une requête avant de remplacer le backend. L'URL JDBC utilise `mysql:3306` sur le réseau privé Docker `senproxi` ; `localhost` désignerait le conteneur backend. La connexion n'est pas chiffrée sur ce réseau local privé : pour une base externalisée, configurer TLS et adapter l'URL.

La configuration initiale utilise `ddl-auto=update` pour créer les tables dans la base vide. Après le premier démarrage réussi, modifier `SPRING_JPA_HIBERNATE_DDL_AUTO=validate` dans `backend.env` et redéployer. Pour les évolutions suivantes, appliquer des migrations contrôlées après sauvegarde. Ne pas exécuter les scripts de conversion des anciennes tables sur cette base neuve.

MySQL et ses données sont conservés aux déploiements suivants. Modifier un mot de passe dans `mysql.env` ne change pas celui d'une base déjà initialisée : effectuer la rotation avec SQL, puis synchroniser les fichiers de configuration. Ne jamais supprimer le volume pour réinitialiser un mot de passe. Prévoir des sauvegardes MySQL régulières hors du VPS ; un volume persistant ne constitue pas une sauvegarde. Les variables d'initialisation suivent le fonctionnement de l'[image officielle MySQL](https://hub.docker.com/_/mysql).

## Déploiement

Le runner transfère l'image compressée par SCP, puis appelle `deploy/release.sh` par SSH. L'image est identifiée par le SHA du commit. Le backend tourne dans `senproxi-backend`, avec l'alias `backend` sur le réseau `senproxi`. Son port est accessible uniquement sur `127.0.0.1:8080` du VPS. Les fichiers envoyés sont conservés dans le volume Docker `senproxi-uploads`.

Le script vérifie `GET /api/categories` après démarrage. En cas d'échec, il tente de restaurer le conteneur précédent et fait échouer le job. Ce retour arrière ne restaure pas la base de données. Le redémarrage entraîne une courte interruption. Les images Docker précédentes sont conservées : prévoir une politique de nettoyage et des sauvegardes du volume et de MySQL.

Exposer le frontend via un reverse proxy HTTPS vers `127.0.0.1:8088`. Le frontend transmet `/api/` au backend via le réseau Docker. Ne pas exposer directement MySQL. Configurer d'abord les deux dépôts et le serveur, déployer le backend, puis le frontend, puis définir `DEPLOY_ENABLED=true` dans les deux dépôts pour les pushs suivants.

## Vérification locale

```powershell
.\mvnw.cmd verify
docker build -t senproxi-backend:local .
```

Ne pas lancer `deploy/publish.sh` localement avec des secrets de production pour un simple test de syntaxe. Les scripts de publication sont conçus pour le runner GitHub Linux.

Références : [setup-java](https://github.com/actions/setup-java), [checkout](https://github.com/actions/checkout).
