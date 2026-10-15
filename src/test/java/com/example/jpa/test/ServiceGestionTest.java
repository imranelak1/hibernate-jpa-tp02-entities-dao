package com.example.jpa.test;

import com.example.jpa.dao.SalleDao;
import com.example.jpa.dao.UtilisateurDao;
import com.example.jpa.entities.Salle;
import com.example.jpa.entities.Utilisateur;
import com.example.jpa.persistence.JpaContext;
import com.example.jpa.service.ServiceGestion;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.Map;
import java.util.UUID;
import static org.junit.Assert.*;

public class ServiceGestionTest {
    private ServiceGestion service;

    @Before
    public void setUp() {
        service = new ServiceGestion(Map.of(
                "javax.persistence.jdbc.url", "jdbc:h2:mem:tp2_" + UUID.randomUUID(),
                "hibernate.hbm2ddl.auto", "create-drop",
                "hibernate.show_sql", "false"));
    }

    @After
    public void tearDown() {
        service.fermer();
    }

    @Test
    public void testCreationEtRechercheSalle() {
        Salle salle = new Salle("Salle Test", 20);
        service.ajouterSalle(salle);

        assertNotNull(salle.getId());
        assertEquals(1, service.listerSalles().size());
        assertEquals("Salle Test", service.trouverSalle(salle.getId()).getNom());
    }

    @Test
    public void testCreationUtilisateurAvecSalle() {
        Salle salle = new Salle("Salle B", 15);
        service.ajouterSalle(salle);
        Utilisateur alice = new Utilisateur("Alice", "alice@example.com", salle);
        service.ajouterUtilisateur(alice);

        assertEquals("Alice", service.listerUtilisateurs().get(0).getNom());
        assertEquals("Salle B", service.trouverUtilisateur(alice.getId()).getSalle().getNom());
    }

    @Test
    public void testRechercheUtilisateursParSalle() {
        Salle salle = new Salle("Salle C", 30);
        service.ajouterSalle(salle);
        service.ajouterUtilisateur(new Utilisateur("Bob", "bob@example.com", salle));
        service.ajouterUtilisateur(new Utilisateur("Carol", "carol@example.com", salle));

        assertEquals(2, service.utilisateursParSalle("Salle C").size());
        assertTrue(service.utilisateursParSalle("Inconnue").isEmpty());
    }

    @Test
    public void testCrudDetacheEtChangementDeSalle() {
        Salle a = new Salle("A", 10);
        Salle b = new Salle("B", 20);
        service.ajouterSalle(a);
        service.ajouterSalle(b);
        Utilisateur user = new Utilisateur("Alice", "alice@example.com", a);
        service.ajouterUtilisateur(user);

        a.setCapacite(12);
        service.modifierSalle(a);
        assertEquals(12, service.trouverSalle(a.getId()).getCapacite());

        user.setSalle(b);
        service.modifierUtilisateur(user);
        assertEquals(b.getId(), service.trouverUtilisateur(user.getId()).getSalle().getId());

        service.supprimerSalle(a);
        assertNull(service.trouverSalle(a.getId()));
        service.supprimerUtilisateur(user);
        assertNull(service.trouverUtilisateur(user.getId()));
    }

    @Test
    public void testBornesCapaciteEtRechercheNomLitterale() {
        Salle a = new Salle("A", 20);
        Salle b = new Salle("B", 30);
        service.ajouterSalle(a);
        service.ajouterSalle(b);

        assertEquals(2, service.sallesParCapaciteMin(20).size());
        assertEquals(1, service.sallesParCapaciteSuperieure(20).size());
        service.ajouterUtilisateur(new Utilisateur("Jean Dupont", "jean@example.com", a));
        service.ajouterUtilisateur(new Utilisateur("Jean_100%", "literal@example.com", a));
        assertEquals(2, service.utilisateursParNom("Jean").size());
        assertEquals(1, service.utilisateursParNom("_100%").size());
    }

    @Test
    public void testUniciteEtRollbackPermettentDeContinuer() {
        Salle a = new Salle("A", 10);
        service.ajouterSalle(a);
        assertThrows(RuntimeException.class, () -> service.ajouterSalle(new Salle("A", 20)));
        service.ajouterUtilisateur(new Utilisateur("Alice", "alice@example.com", a));
        assertThrows(RuntimeException.class, () ->
                service.ajouterUtilisateur(new Utilisateur("Doublon", "alice@example.com", a)));
        assertEquals(1, service.listerSalles().size());
        assertEquals(1, service.listerUtilisateurs().size());

        service.ajouterUtilisateur(new Utilisateur("Bob", "bob@example.com", a));
        assertEquals(2, service.listerUtilisateurs().size());
    }

    @Test
    public void testCleEtrangereEmpecheSuppressionSalleOccupee() {
        Salle a = new Salle("A", 10);
        service.ajouterSalle(a);
        service.ajouterUtilisateur(new Utilisateur("Alice", "alice@example.com", a));

        assertThrows(RuntimeException.class, () -> service.supprimerSalle(a));
        assertNotNull(service.trouverSalle(a.getId()));
        assertEquals(1, service.listerUtilisateurs().size());
    }

    @Test
    public void testContraintesMetierEtNullEnBase() {
        assertThrows(IllegalArgumentException.class, () -> new Salle("A", 0));
        assertThrows(IllegalArgumentException.class, () -> new Utilisateur("", "a@example.com", null));
        assertThrows(RuntimeException.class, () -> service.ajouterSalle(new Salle()));
        assertTrue(service.listerSalles().isEmpty());
    }

    /** Verifie la liste et le comptage pendant un cycle CRUD complet. */
    @Test
    public void testGenericDaoFindAllEtCountPendantLeCrud() {
        EntityManagerFactory factory = nouvelleFactoryDao();
        EntityManager em = factory.createEntityManager();
        try {
            SalleDao dao = new SalleDao(em);
            assertEquals(0L, dao.count());
            assertTrue(dao.findAll().isEmpty());

            Salle a = new Salle("A", 10);
            Salle b = new Salle("B", 20);
            dao.create(a);
            dao.create(b);
            assertEquals(2L, dao.count());
            assertEquals(a.getId(), dao.findAll().get(0).getId());
            assertEquals(b.getId(), dao.findAll().get(1).getId());

            em.clear(); // Le service utilise aussi des entites detachees entre les appels.
            a.setCapacite(15);
            dao.update(a);
            em.clear();
            assertEquals(15, dao.find(a.getId()).getCapacite());
            assertEquals(2L, dao.count());

            dao.delete(a);
            assertNull(dao.find(a.getId()));
            assertEquals(1L, dao.count());
            assertEquals(b.getId(), dao.findAll().get(0).getId());
            dao.delete(b);
            assertEquals(0L, dao.count());
            assertTrue(dao.findAll().isEmpty());
        } finally {
            em.close();
            factory.close();
        }
    }

    /** Un email duplique annule l'ecriture et laisse la DAO utilisable. */
    @Test
    public void testRollbackEmailDupliqueSansModificationPartielle() {
        EntityManagerFactory factory = nouvelleFactoryDao();
        EntityManager em = factory.createEntityManager();
        try {
            UtilisateurDao dao = new UtilisateurDao(em);
            Utilisateur alice = new Utilisateur("Alice", "alice@example.com", null);
            dao.create(alice);

            assertThrows(RuntimeException.class, () ->
                    dao.create(new Utilisateur("Doublon", "alice@example.com", null)));
            assertFalse(em.getTransaction().isActive());
            assertFalse(em.contains(alice));
            assertEquals(1L, dao.count());
            assertEquals("Alice", dao.find(alice.getId()).getNom());

            Utilisateur bob = new Utilisateur("Bob", "bob@example.com", null);
            dao.create(bob); // Une nouvelle transaction fonctionne apres le rollback.
            bob.setNom("Nom qui ne doit pas etre conserve");
            bob.setEmail("alice@example.com");
            assertThrows(RuntimeException.class, () -> dao.update(bob));
            assertFalse(em.getTransaction().isActive());
            assertFalse(em.contains(bob));

            Utilisateur bobEnBase = dao.find(bob.getId());
            assertEquals("Bob", bobEnBase.getNom());
            assertEquals("bob@example.com", bobEnBase.getEmail());
            assertEquals("Alice", dao.find(alice.getId()).getNom());
            assertEquals("alice@example.com", dao.find(alice.getId()).getEmail());
            assertEquals(2L, dao.count());
            assertEquals(2, dao.findAll().size());

            bobEnBase.setNom("Bob modifie");
            dao.update(bobEnBase);
            dao.create(new Utilisateur("Charlie", "charlie@example.com", null));
            em.clear();
            assertEquals("Bob modifie", dao.find(bob.getId()).getNom());
            assertEquals(3L, dao.count());
        } finally {
            em.close();
            factory.close();
        }
    }

    private EntityManagerFactory nouvelleFactoryDao() {
        return Persistence.createEntityManagerFactory("gestionPU", Map.of(
                "javax.persistence.jdbc.url", "jdbc:h2:mem:dao_" + UUID.randomUUID(),
                "hibernate.hbm2ddl.auto", "create-drop", "hibernate.show_sql", "false"));
    }

    /** Fermer un service ne doit pas fermer la factory partagee par les autres. */
    @Test
    public void testFactorySingletonPartageeEntreServices() {
        try (ServiceGestion premier = new ServiceGestion();
             ServiceGestion second = new ServiceGestion()) {
            Salle salle = new Salle("Partage_" + UUID.randomUUID(), 10);
            try {
                premier.ajouterSalle(salle);
                premier.fermer();
                assertTrue(JpaContext.getEntityManagerFactory().isOpen());
                assertThrows(IllegalStateException.class, premier::listerSalles);

                Salle retrouvee = second.trouverSalle(salle.getId());
                assertEquals(salle.getNom(), retrouvee.getNom());
                retrouvee.setCapacite(25);
                second.modifierSalle(retrouvee);
                assertEquals(25, second.trouverSalle(salle.getId()).getCapacite());
            } finally {
                if (salle.getId() != null && second.trouverSalle(salle.getId()) != null) {
                    second.supprimerSalle(salle);
                }
            }
        }
        assertTrue(JpaContext.getEntityManagerFactory().isOpen());
    }
}
