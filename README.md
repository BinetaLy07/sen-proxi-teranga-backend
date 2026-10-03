# Sen Proxi Teranga — Backend

API Spring Boot, Java 21 et MySQL. Les utilisateurs partagent une table et sont distingués par leur rôle : `CLIENT`, `PROFESSIONNEL` ou `ADMINISTRATEUR`.

## Démarrer

Configurer MySQL et les paramètres de `src/main/resources/application.yaml`. Pour une base existante, lire les migrations documentées avant le démarrage. Fournir `ADMIN_PASSWORD` dans l'environnement lorsque le compte administrateur initial est absent ; ne pas versionner de secret.

```powershell
.\mvnw.cmd spring-boot:run
```

L'API écoute sur `http://localhost:8080`. Le frontend Angular utilise un proxy `/api` en développement.

## Guides

- [Authentification, rôles, sessions et migrations](docs/securite.md).
- [Sécuriser un nouvel endpoint](docs/SECURISER-ENDPOINT.md) : règles HTTP, `@PreAuthorize`, propriétaire des données, validation et tests.
- [Collection Postman de sécurité](docs/postman/README.md).

## Vérifier

```powershell
.\mvnw.cmd test
```

Les vérifications automatisées ne remplacent pas un essai de bout en bout avec MySQL et le frontend réels.
