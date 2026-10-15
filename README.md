# TP 2 — Gestion des salles et utilisateurs

J'ai utilisé JPA et Hibernate pour gérer des salles et leurs utilisateurs, sans EJB. Les données sont stockées dans une base H2 en mémoire.

Le `GenericDao` contient le CRUD, `findAll()` et `count()`. Les recherches JPQL filtrent les salles par capacité et les utilisateurs par salle ou par nom. Les services partagent la factory de `JpaContext` et ferment leur `EntityManager` après chaque opération.

La démo montre aussi la modification d'objets détachés et le refus d'un email dupliqué. La transaction est annulée sans modifier les données existantes.

Avec Java 17 et Maven :

```sh
mvn clean verify
mvn compile exec:java
```

Les **11 tests** vérifient le CRUD, les contraintes, les recherches, le rollback et la réutilisation de la factory.

Les captures viennent des exécutions réelles dans le terminal.

![Résultats de la démo](screenshots/results.png)

![Résultats des tests](screenshots/tests.png)
