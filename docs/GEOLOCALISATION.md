# Géolocalisation

Les colonnes nullable `utilisateurs.latitude` et `utilisateurs.longitude` représentent
le lieu d’activité public du professionnel, sans créer une autre table utilisateur.
Le déploiement actuel utilise Hibernate `update` pour ajouter ces deux colonnes.
Avec un schéma géré manuellement, ajouter deux colonnes `DOUBLE NULL` avant de déployer.

- `GET /api/professionnels/{id}/position` : position enregistrée, ou deux valeurs null.
- `PUT /api/professionnels/{id}/position` : JSON `{"latitude":14.7167,"longitude":-17.4677}`.
- `DELETE /api/professionnels/{id}/position` : retire la position publique.

Ces routes utilisent `EndpointAccess` : uniquement le professionnel concerné ou
l’administrateur. Une latitude doit être comprise entre -90 et 90, une longitude
entre -180 et 180 ; les deux valeurs sont obligatoires lors de l’enregistrement.

`GET /api/proximite/professionnels?latitude=14.7167&longitude=-17.4677&rayonKm=10`
est réservé au rôle CLIENT. Les filtres `metier`, `zoneId` et `categorieId` sont
facultatifs. Le rayon accepté va de 1 à 100 km (10 par défaut).
La réponse contient `professionnel` (résumé habituel), `latitude`, `longitude`,
et `distanceKm` à vol d’oiseau, triée du plus proche au plus éloigné.
Les professionnels sans coordonnées, non validés, suspendus ou hors rayon sont exclus.

La recherche réutilise les filtres du dépôt et calcule la distance de Haversine
en mémoire. Pour un grand catalogue, déplacer le filtrage géographique et la
pagination en base avec un index spatial. La position du client n’est pas
persistée dans la base ; les paramètres HTTP peuvent figurer dans les journaux
techniques selon la configuration du proxy.
