# TP 2 — Entités Salle et Utilisateur, DAO et services JPA

Dans ce TP, j'ai utilisé JPA et Hibernate pour gérer des salles et des utilisateurs. Chaque utilisateur peut être associé à une salle.

Le `GenericDao` regroupe le CRUD, `findAll()` et `count()`. Les services partagent la factory de `JpaContext`, avec un `EntityManager` par opération. En cas d'erreur, la DAO annule la transaction et vide le contexte.

La démo montre les recherches par capacité et par nom, puis un email dupliqué refusé avec rollback. À la fin, il reste une salle et deux utilisateurs.

Pour lancer le projet, il faut Java 17 et Maven. La base H2 fonctionne en mémoire.

```sh
mvn clean test
mvn compile exec:java
```

Les résultats enregistrés donnent **11 tests réussis, sans échec ni erreur**, dont la vérification qu'un email dupliqué ne laisse aucune modification partielle.

Les captures de la démo et des tests viennent d’exécutions réelles dans le terminal.

![Résultats de la démo](screenshots/results.png)

![Résultats des tests](screenshots/tests.png)
