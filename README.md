# TP 2 — Entités Salle et Utilisateur, DAO et services JPA

Projet autonome du cours **Hibernate & JPA**, laboratoire 145. Il gère les salles et leurs utilisateurs avec des DAO génériques, des requêtes JPQL et des transactions locales, dans une application Java SE sans EJB ni serveur d'application.

## Exécuter

Prérequis : **JDK 17** et **Maven 3.9+**. Importer le fichier `pom.xml` dans un IDE comme projet Maven.

```sh
mvn clean test
mvn compile exec:java
```

La classe principale est `com.example.jpa.App`. La démonstration se termine automatiquement et chaque lancement Java utilise une base H2 en mémoire neuve.

## Toutes les étapes du laboratoire

| Étape | Réalisation |
| --- | --- |
| Création Maven | Projet `com.example.jpa:gestion-salles:1.0-SNAPSHOT`, dossiers `src/main/java`, `src/main/resources` et `src/test/java` |
| Dépendances exactes | `hibernate-entitymanager:5.6.15.Final`, `javax.persistence-api:2.2`, H2 `2.1.214` en runtime, JUnit `4.13.2` en test |
| Configuration JPA | Unité `gestionPU`, transactions `RESOURCE_LOCAL`, base `jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1`, dialecte H2, schéma `update` et SQL formaté |
| Entité Salle | `com.example.jpa.entities.Salle`, ID auto-généré, nom non nul et unique, capacité non nulle |
| Entité Utilisateur | `com.example.jpa.entities.Utilisateur`, ID auto-généré, nom non nul, email non nul et unique |
| Relation | `Utilisateur.salle` en `ManyToOne`, clé étrangère `salle_id` |
| DAO générique | `GenericDao<T>` : `create`, `find`, `update`, `delete`, `findAll` ; résultats et classe d'entité typés |
| SalleDao | `findByCapaciteMin` avec `>=` et paramètres nommés |
| UtilisateurDao | `findBySalleNom` avec navigation JPQL `u.salle.nom` |
| Transactions | `EntityTransaction.begin/commit`, rollback des transactions actives en cas d'exception |
| Service métier | `ServiceGestion` expose créations, recherches, modifications, suppressions et listes |
| Tests JUnit | Tests demandés de création/recherche de salle, utilisateur avec salle et utilisateurs par salle, plus contrôles CRUD et erreurs |
| Erreurs et logs | Exception propagée, transaction annulée, contexte vidé puis fermé ; journal de rollback |
| JPQL avancé | Capacité strictement supérieure avec `>` et nom contenant une chaîne avec `LIKE` paramétré |
| Ressources | Factory partagée ; EntityManager court, fermé après chaque opération ; service `AutoCloseable` et méthode `fermer` |

Les classes gardent les noms et packages demandés. Les génériques manquants dans les extraits du TP sont explicités (`GenericDao<T>`, `Class<T>`, `List<T>`). L'artefact Hibernate EntityManager est conservé comme dans l'énoncé ; Maven résout son implémentation Hibernate Core correspondante.

## Modèle et règles

Une salle accueille plusieurs utilisateurs ; chaque utilisateur référence au plus une salle. La relation n'a pas de cascade de suppression : supprimer une salle encore référencée est interdit par la clé étrangère. Les utilisateurs doivent être supprimés ou déplacés avant de supprimer cette salle.

`SalleDao.findByCapaciteMin(20)` inclut les salles de capacité 20 ; `findByCapaciteSuperieure(20)` les exclut. `UtilisateurDao.findByNomContains` cherche une sous-chaîne littérale : `%`, `_` et `!` sont échappés et la valeur est liée à un paramètre JPQL. La recherche conserve la sensibilité à la casse de la comparaison H2 utilisée ici.

Chaque appel service ouvre puis ferme un EntityManager. Les objets renvoyés sont détachés ; `update` utilise `merge` et renvoie l'instance fusionnée. `delete` accepte également une instance détachée. La factory est créée une fois et fermée via `try-with-resources` ou `fermer()`.

## Démonstration et résultats

L'application crée Salle A de capacité 20 et Salle B de capacité 30, puis Alice, Jean Dupont et Bob. Elle affiche les listes, les utilisateurs de Salle B, les deux variantes du filtre de capacité et le filtre de nom `Jean`.

Elle modifie ensuite la capacité de Salle A à 25, déplace Alice vers Salle B, refuse un email dupliqué avec rollback et supprime Bob puis Salle A. **État final vérifié : 1 salle et 2 utilisateurs**. Les requêtes SQL montrent les insertions, les recherches, les mises à jour et les suppressions réellement effectuées.

## Validation

**8 tests JUnit 4 réussis, 0 échec, 0 erreur, 0 test ignoré.** Chaque méthode utilise une base H2 distincte avec un schéma `create-drop` ; les tests couvrent :

- les trois scénarios de persistance demandés dans l'énoncé ;
- le CRUD avec objets détachés et le changement de salle ;
- les bornes de capacité et les caractères littéraux dans `LIKE` ;
- l'unicité du nom de salle et de l'email, le rollback et la reprise après erreur ;
- la clé étrangère empêchant la suppression d'une salle occupée ;
- les contraintes métier et le refus d'une salle non renseignée en base.

Les messages SQL d'erreur d'unicité et de clé étrangère dans les traces correspondent à des **cas négatifs attendus et vérifiés** par les tests, puis suivis d'assertions sur l'état conservé.

Preuves d'exécution réelles : [tests](evidence/test-output.txt), [démonstration](evidence/demo-output.txt), [rapports JUnit XML](evidence/junit/), [rapport consultable](evidence/report.html).

Les captures ci-dessous sont des **rapports de navigateur présentant les journaux et résultats JUnit conservés**, et non des captures directes du terminal.

![Résultats de la démonstration](screenshots/results.png)
![Résultats des huit tests](screenshots/tests.png)

## Références

- [Guide officiel Hibernate ORM 5.6](https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html)
- [Tutoriel officiel Oracle : JPA](https://docs.oracle.com/javaee/7/tutorial/persistence-intro.htm)
- [Documentation JUnit 4](https://junit.org/junit4/)


## Régénérer le rapport et les captures

Les images sont des captures navigateur de résultats réellement exécutés, conservés dans `evidence/`. Les tests proviennent des XML Surefire, également archivés dans `evidence/junit/`. Les empreintes SHA-256 des sources figurent dans `evidence/checksums.json`.

Avec Python 3 et Node.js disponibles, depuis la racine du dépôt :

```sh
mvn clean verify > evidence/test-output.txt 2>&1
mvn exec:java > evidence/demo-output.txt 2>&1
python scripts/generate_report.py
npm install --no-save playwright
npx playwright install chromium
node scripts/capture-results.cjs
```

Le rapport se consulte aussi directement dans `evidence/report.html`. Les captures générées sont `screenshots/results.png` et `screenshots/tests.png`.
