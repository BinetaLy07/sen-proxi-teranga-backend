# Collection Postman — Sécurité

Importer les deux fichiers JSON de ce dossier dans Postman (Import), puis sélectionner l’environnement **Sen Proxi Teranga — Local**. Le backend doit tourner sur `http://localhost:8080` ; modifier `baseUrl` si nécessaire. La migration du modèle utilisateur doit être terminée si la base contient les anciennes tables.

## Exécution

1. Exécuter le dossier **01 — Inscription et connexion** dans l’ordre. Les requêtes créent un client et un professionnel de test avec des emails et téléphones générés. Les identifiants, tokens et la session professionnel sont enregistrés automatiquement.
2. Exécuter **02 — Renouvellement et contrôles**. Le refresh met à jour les deux tokens et conserve les anciens pour tester leur refus. Les contrôles attendent volontairement 400, 401 ou 403.
3. Pour **03 — Administration**, renseigner `adminEmail` et `adminPassword` avec un administrateur existant dans l’environnement. Le compte initial est créé au démarrage si absent, avec la configuration `ADMIN_*` décrite dans `docs/securite.md`. Le dossier crée un autre administrateur de test, suspend le client créé dans 01, vérifie le refus des tokens et de la connexion, puis le réactive et le reconnecte. L’endpoint administrateur ne permet pas de créer le premier administrateur sans authentification.
4. Exécuter **04 — Sessions et déconnexion** dans l’ordre. Il teste les sessions, la propriété d’une session, la révocation, la déconnexion et la déconnexion globale. Il récupère automatiquement `sessionId` avant suppression.

Sans administrateur existant, sélectionner uniquement les dossiers **01, 02 et 04** dans le Collection Runner. Avec un administrateur configuré, exécuter les quatre dossiers dans l’ordre. Pour relancer tout le scénario, commencer par 01 : les nouveaux emails évitent les doublons. Les comptes créés restent en base ; les téléphones aléatoires ont un faible risque de collision, auquel cas relancer l’inscription concernée.

Les tokens sont des variables **de collection**, les paramètres locaux administrateur sont des variables **d’environnement**. Ne pas créer de variables d’environnement portant les mêmes noms que les tokens, car elles masqueraient les valeurs mises à jour par les scripts. Les mots de passe des comptes générés sont des exemples réservés aux tests locaux. Les fichiers livrés ne contiennent aucun token ni identifiant administrateur réel.

Les requêtes comportent des assertions de code HTTP et des vérifications du rôle, du statut ACTIF à la création et de la rotation des tokens. Les erreurs d’autorisation peuvent avoir un corps vide : les assertions 401/403 ne supposent pas un format JSON particulier.

## Vérification des fichiers

La collection et l’environnement ont été relus et la collection validée contre le schéma Postman v2.1. Les scénarios ne sont pas exécutés contre une base réelle lors de leur génération.
