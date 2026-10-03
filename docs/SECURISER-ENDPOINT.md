# Sécuriser un nouvel endpoint Spring

## Fonctionnement actuel

`SecurityConfig` active `@EnableMethodSecurity`, configure une chaîne stateless et installe `BearerSessionFilter`. Le client fournit `Authorization: Bearer <accessToken>`. Les tokens sont opaques, pas des JWT : `SessionService` vérifie la session persistée, l'expiration et le compte. Le principal est `SessionPrincipal(utilisateurId, sessionId, role)`.

Les autorités utilisent le préfixe `ROLE_`. Écrire `hasRole('ADMINISTRATEUR')`, `hasRole('CLIENT')` ou `hasRole('PROFESSIONNEL')`, sans ajouter ce préfixe. Les rôles sont stockés en base ; ne jamais accepter comme preuve un rôle ou un identifiant envoyé dans le corps d'une requête.

La dernière règle HTTP est `anyRequest().denyAll()`. Un controller et son `@PreAuthorize` ne suffisent donc pas : il faut aussi autoriser son chemin dans la chaîne HTTP. La première règle HTTP correspondante s'applique ; placer les règles spécifiques avant les règles générales.

## 1. Décider qui peut appeler la route

| Besoin | Règle HTTP / contrôle |
| --- | --- |
| Lecture publique | permitAll sur la méthode et le chemin exacts |
| Tout compte connecté | authenticated + contrôle des données si nécessaire |
| Administration | hasRole("ADMINISTRATEUR") ou authenticated + @PreAuthorize |
| Client ou professionnel | rôle + propriétaire / participant de la ressource |

Éviter un `permitAll` global sur `/api/**`. Définir séparément les méthodes de lecture et de modification. Ne pas rendre toutes les routes d'un controller publiques parce qu'une seule doit l'être.

## 2. Ajouter une règle HTTP

Exemple pédagogique : une future route `GET /api/admin/rapports`. Dans `authorizeHttpRequests`, avant `anyRequest().denyAll()` et avant toute règle générale susceptible de correspondre :

```java
.requestMatchers(HttpMethod.GET, "/api/admin/rapports")
.authenticated()
```

Puis sur la méthode du controller :

```java
@GetMapping("/api/admin/rapports")
@PreAuthorize("hasRole('ADMINISTRATEUR')")
public List<RapportResponse> listerRapports() {
    return rapportService.lister();
}
```

Cet exemple suppose un controller sans préfixe : avec `@RequestMapping("/api/admin")`, écrire seulement `@GetMapping("/rapports")`. Créer les DTO et service correspondants avant de copier l'exemple. Importer `org.springframework.security.access.prepost.PreAuthorize`.

On peut aussi mettre directement `.hasRole("ADMINISTRATEUR")` dans la règle HTTP. Le projet utilise déjà ce choix pour la création administrative, tandis que le changement de statut utilise `.authenticated()` puis `@PreAuthorize` sur la méthode. Les deux contrôles, lorsqu'ils coexistent, doivent être satisfaits.

## 3. Contrôler le propriétaire de la ressource

Un rôle seul ne suffit pas : deux clients ne doivent pas lire ou modifier les demandes l'un de l'autre. Récupérer le principal fourni par Spring plutôt que faire confiance au `clientId` du navigateur :

```java
@GetMapping("/api/mes-demandes")
@PreAuthorize("hasRole('CLIENT')")
public List<DemandeResponse> mesDemandes(
        @AuthenticationPrincipal SessionPrincipal principal) {
    return demandeService.listerParClient(principal.utilisateurId());
}
```

Ajouter aussi la règle HTTP correspondante `.requestMatchers(HttpMethod.GET, "/api/mes-demandes").authenticated()`. Importer `AuthenticationPrincipal` depuis `org.springframework.security.core.annotation` et `SessionPrincipal` depuis le package `security` du projet.

Pour une route contenant un identifiant de ressource, charger la ressource et comparer son propriétaire ou ses participants avec `principal.utilisateurId()` avant toute lecture sensible ou modification. Dans le service transactionnel, refuser avec `AccessDeniedException` si le compte n'a pas accès. Les dépôts peuvent aussi filtrer directement par identifiant et propriétaire. Toute exception pour l'administrateur doit être un choix métier explicite.

Les URL actuelles `/api/clients/**`, `/api/professionnels/**` et `/api/demandes/**` passent par `EndpointAccess` : identité du client/professionnel ou participation à la demande, avec accès administrateur. Vérifier aussi dans le service qu'une ressource imbriquée appartient à l'utilisateur de l'URL. Par exemple, un `clientId` correct ne prouve pas qu'un `demandeId` fourni appartient à ce client.

`EndpointAccess` reçoit la méthode HTTP mais ne la distingue pas actuellement. Une nouvelle action exigeant un rôle particulier doit avoir une règle plus précise avant ces matchers, ou un contrôle de méthode/service complémentaire. Ne pas supposer que ce composant connaît automatiquement les permissions d'une nouvelle action.

## 4. Valider les entrées et les transitions

Employer un DTO dédié avec contraintes Bean Validation et `@Valid @RequestBody`. Ne pas accepter directement une entité JPA : le client ne doit pas pouvoir modifier librement le rôle, le statut du compte ou le mot de passe encodé.

Vérifier les transitions dans le service (demande annulée, devis déjà accepté, compte suspendu, etc.). Les nouveaux comptes sont actifs par défaut ; le changement de `StatutCompte` est réservé à l'administrateur et la suspension révoque les sessions. Ne pas exposer les hashes de mots de passe ou de tokens dans les réponses et journaux.

La désactivation de CSRF actuelle correspond à l'authentification par Bearer envoyé explicitement dans un header. Si l'authentification passe ultérieurement par cookies envoyés automatiquement, revoir la protection CSRF. Le frontend utilise un proxy local et doit exposer `/api` sur le même domaine en production ; une origine différente nécessite une configuration CORS explicite.

## 5. Tester la sécurité

S'appuyer sur `src/test/java/sn/senproxiteranga/backend/security/SessionSecurityTests.java`. Tester le filtre avec de vrais tokens de sessions de test ; un simple `@WithMockUser` ne reproduit pas un `SessionPrincipal` ni l'expiration ou la révocation des tokens.

Pour chaque nouvelle route, couvrir les cas suivants selon le contrat :

- Sans token ou token expiré/révoqué : 401.
- Compte connecté avec rôle interdit : 403.
- Bon rôle mais ressource appartenant à quelqu'un d'autre : refus sans fuite de données.
- Bon rôle et bon propriétaire : succès et modification attendue.
- Données invalides : 400 ; ressource absente : réponse conforme au contrat.
- Après suspension ou déconnexion : ancien token refusé.

```powershell
.\mvnw.cmd test
```

Ajouter ensuite la requête à la collection `docs/postman` avec le header `Authorization: Bearer {{accessToken}}` pour les routes protégées. Les routes publiques de connexion, inscription et refresh n'exigent pas de Bearer. Après une rotation, utiliser les deux nouveaux tokens : l'ancien refresh token n'est plus valable.

## Administration initiale

`RoleInitialization` crée uniquement les rôles absents et le compte administrateur initial absent. Fournir `ADMIN_PASSWORD` au démarrage si ce compte n'existe pas. Les paramètres optionnels sont `ADMIN_EMAIL`, `ADMIN_TELEPHONE`, `ADMIN_PRENOM` et `ADMIN_NOM`. Un compte existant n'est pas recréé, et un utilisateur non administrateur correspondant n'est pas promu automatiquement. Conserver les secrets hors du dépôt. Lire les documents de migration dans `docs` avant d'appliquer ces changements à une base existante.
