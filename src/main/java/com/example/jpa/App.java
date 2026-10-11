package com.example.jpa;

import com.example.jpa.entities.Salle;
import com.example.jpa.entities.Utilisateur;
import com.example.jpa.service.ServiceGestion;

public final class App {
    private App() { }
    public static void main(String[] args) {
        try (ServiceGestion service = new ServiceGestion()) {
            Salle a = new Salle("Salle A", 20), b = new Salle("Salle B", 30);
            service.ajouterSalle(a); service.ajouterSalle(b);
            Utilisateur alice = new Utilisateur("Alice", "alice@example.com", a);
            Utilisateur jean = new Utilisateur("Jean Dupont", "jean@example.com", b);
            Utilisateur bob = new Utilisateur("Bob", "bob@example.com", b);
            service.ajouterUtilisateur(alice); service.ajouterUtilisateur(jean); service.ajouterUtilisateur(bob);
            System.out.println("\nTP2 - Salles : " + service.listerSalles());
            System.out.println("Utilisateurs : " + service.listerUtilisateurs());
            System.out.println("Utilisateurs de Salle B : " + service.utilisateursParSalle("Salle B"));
            System.out.println("Capacite >= 20 : " + service.sallesParCapaciteMin(20));
            System.out.println("Capacite > 20 : " + service.sallesParCapaciteSuperieure(20));
            System.out.println("Nom contenant Jean : " + service.utilisateursParNom("Jean"));
            a.setCapacite(25); service.modifierSalle(a);
            alice.setSalle(b); service.modifierUtilisateur(alice);
            System.out.println("Mise a jour Salle A : " + service.trouverSalle(a.getId()));
            System.out.println("Alice apres changement de salle : " + service.trouverUtilisateur(alice.getId()));
            try { service.ajouterUtilisateur(new Utilisateur("Doublon", "alice@example.com", b)); }
            catch (RuntimeException expected) { System.out.println("Email duplique refuse : transaction annulee, utilisateurs=" + service.listerUtilisateurs().size()); }
            service.supprimerUtilisateur(bob); service.supprimerSalle(a);
            System.out.println("Apres suppression : salles=" + service.listerSalles().size() + ", utilisateurs=" + service.listerUtilisateurs().size());
            System.out.println("TP2 RESULTAT : CRUD, ManyToOne, JPQL et rollback verifies sans EJB.");
        }
    }
}
