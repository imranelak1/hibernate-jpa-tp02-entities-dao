package com.example.jpa.test;

import com.example.jpa.entities.Salle;
import com.example.jpa.entities.Utilisateur;
import com.example.jpa.service.ServiceGestion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.Map;
import java.util.UUID;
import static org.junit.Assert.*;

public class ServiceGestionTest {
    private ServiceGestion service;
    @Before public void setUp() {
        service = new ServiceGestion(Map.of("javax.persistence.jdbc.url", "jdbc:h2:mem:tp2_" + UUID.randomUUID(),
                "hibernate.hbm2ddl.auto", "create-drop", "hibernate.show_sql", "false"));
    }
    @After public void tearDown() { service.fermer(); }
    @Test public void testCreationEtRechercheSalle() {
        Salle salle = new Salle("Salle Test", 20); service.ajouterSalle(salle);
        assertNotNull(salle.getId()); assertEquals(1, service.listerSalles().size());
        assertEquals("Salle Test", service.trouverSalle(salle.getId()).getNom());
    }
    @Test public void testCreationUtilisateurAvecSalle() {
        Salle salle = new Salle("Salle B", 15); service.ajouterSalle(salle);
        Utilisateur alice = new Utilisateur("Alice", "alice@example.com", salle); service.ajouterUtilisateur(alice);
        assertEquals("Alice", service.listerUtilisateurs().get(0).getNom());
        assertEquals("Salle B", service.trouverUtilisateur(alice.getId()).getSalle().getNom());
    }
    @Test public void testRechercheUtilisateursParSalle() {
        Salle salle = new Salle("Salle C", 30); service.ajouterSalle(salle);
        service.ajouterUtilisateur(new Utilisateur("Bob", "bob@example.com", salle));
        service.ajouterUtilisateur(new Utilisateur("Carol", "carol@example.com", salle));
        assertEquals(2, service.utilisateursParSalle("Salle C").size());
        assertTrue(service.utilisateursParSalle("Inconnue").isEmpty());
    }
    @Test public void testCrudDetacheEtChangementDeSalle() {
        Salle a = new Salle("A", 10), b = new Salle("B", 20); service.ajouterSalle(a); service.ajouterSalle(b);
        Utilisateur user = new Utilisateur("Alice", "alice@example.com", a); service.ajouterUtilisateur(user);
        a.setCapacite(12); service.modifierSalle(a); assertEquals(12, service.trouverSalle(a.getId()).getCapacite());
        user.setSalle(b); service.modifierUtilisateur(user); assertEquals(b.getId(), service.trouverUtilisateur(user.getId()).getSalle().getId());
        service.supprimerSalle(a); assertNull(service.trouverSalle(a.getId()));
        service.supprimerUtilisateur(user); assertNull(service.trouverUtilisateur(user.getId()));
    }
    @Test public void testBornesCapaciteEtRechercheNomLitterale() {
        Salle a = new Salle("A", 20), b = new Salle("B", 30); service.ajouterSalle(a); service.ajouterSalle(b);
        assertEquals(2, service.sallesParCapaciteMin(20).size()); assertEquals(1, service.sallesParCapaciteSuperieure(20).size());
        service.ajouterUtilisateur(new Utilisateur("Jean Dupont", "jean@example.com", a));
        service.ajouterUtilisateur(new Utilisateur("Jean_100%", "literal@example.com", a));
        assertEquals(2, service.utilisateursParNom("Jean").size()); assertEquals(1, service.utilisateursParNom("_100%").size());
    }
    @Test public void testUniciteEtRollbackPermettentDeContinuer() {
        Salle a = new Salle("A", 10); service.ajouterSalle(a);
        assertThrows(RuntimeException.class, () -> service.ajouterSalle(new Salle("A", 20)));
        service.ajouterUtilisateur(new Utilisateur("Alice", "alice@example.com", a));
        assertThrows(RuntimeException.class, () -> service.ajouterUtilisateur(new Utilisateur("Doublon", "alice@example.com", a)));
        assertEquals(1, service.listerSalles().size()); assertEquals(1, service.listerUtilisateurs().size());
        service.ajouterUtilisateur(new Utilisateur("Bob", "bob@example.com", a)); assertEquals(2, service.listerUtilisateurs().size());
    }
    @Test public void testCleEtrangereEmpecheSuppressionSalleOccupee() {
        Salle a = new Salle("A", 10); service.ajouterSalle(a);
        service.ajouterUtilisateur(new Utilisateur("Alice", "alice@example.com", a));
        assertThrows(RuntimeException.class, () -> service.supprimerSalle(a));
        assertNotNull(service.trouverSalle(a.getId())); assertEquals(1, service.listerUtilisateurs().size());
    }
    @Test public void testContraintesMetierEtNullEnBase() {
        assertThrows(IllegalArgumentException.class, () -> new Salle("A", 0));
        assertThrows(IllegalArgumentException.class, () -> new Utilisateur("", "a@example.com", null));
        assertThrows(RuntimeException.class, () -> service.ajouterSalle(new Salle()));
        assertTrue(service.listerSalles().isEmpty());
    }
}
