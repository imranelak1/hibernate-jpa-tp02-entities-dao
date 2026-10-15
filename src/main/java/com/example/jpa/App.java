package com.example.jpa;

import com.example.jpa.entities.Salle;
import com.example.jpa.entities.Utilisateur;
import com.example.jpa.service.ServiceGestion;
import com.example.jpa.persistence.JpaContext;

/** Démonstration du CRUD, des recherches et du rollback sur un email dupliqué. */
public final class App {
    private App() {
    }

    public static void main(String[] args) {
        try (ServiceGestion service = new ServiceGestion()) {
            Salle salleA = new Salle("Salle A", 20);
            Salle salleB = new Salle("Salle B", 30);
            service.ajouterSalle(salleA);
            service.ajouterSalle(salleB);

            Utilisateur alice = new Utilisateur("Alice", "alice@example.com", salleA);
            Utilisateur jean = new Utilisateur("Jean Dupont", "jean@example.com", salleB);
            Utilisateur bob = new Utilisateur("Bob", "bob@example.com", salleB);
            service.ajouterUtilisateur(alice);
            service.ajouterUtilisateur(jean);
            service.ajouterUtilisateur(bob);

            System.out.println("\nTP2 - Salles : " + service.listerSalles());
            System.out.println("Utilisateurs : " + service.listerUtilisateurs());
            System.out.println("Utilisateurs de Salle B : " + service.utilisateursParSalle("Salle B"));
            System.out.println("Capacite >= 20 : " + service.sallesParCapaciteMin(20));
            System.out.println("Capacite > 20 : " + service.sallesParCapaciteSuperieure(20));
            System.out.println("Nom contenant Jean : " + service.utilisateursParNom("Jean"));
            // Les EntityManager précédents sont fermés : les objets sont détachés.
            salleA.setCapacite(25);
            service.modifierSalle(salleA);
            alice.setSalle(salleB);
            service.modifierUtilisateur(alice);
            System.out.println("Mise a jour Salle A : " + service.trouverSalle(salleA.getId()));
            System.out.println("Alice apres changement de salle : " + service.trouverUtilisateur(alice.getId()));

            long avantDoublon = service.compterUtilisateurs();
            try {
                service.ajouterUtilisateur(new Utilisateur("Doublon", "alice@example.com", salleB));
                throw new AssertionError("L'email duplique aurait du etre refuse");
            } catch (RuntimeException expected) {
                // Le comptage et la relecture vérifient les données après le rollback.
                if (service.compterUtilisateurs() != avantDoublon
                        || !"Alice".equals(service.trouverUtilisateur(alice.getId()).getNom())) {
                    throw new AssertionError("Le rollback n'a pas preserve les donnees", expected);
                }
                System.out.println("Email duplique refuse : transaction annulee, utilisateurs=" + service.compterUtilisateurs());
                System.out.println("Rollback verifie : Alice conservee, aucun doublon en base.");
            }
            service.supprimerUtilisateur(bob);
            service.supprimerSalle(salleA);
            System.out.println("Apres suppression : salles=" + service.compterSalles()
                    + ", utilisateurs=" + service.compterUtilisateurs());
            System.out.println("TP2 RESULTAT : CRUD, ManyToOne, JPQL et rollback verifies sans EJB.");
        } finally {
            JpaContext.close();
        }
    }
}
