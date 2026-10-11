package com.example.jpa.service;

import com.example.jpa.dao.SalleDao;
import com.example.jpa.dao.UtilisateurDao;
import com.example.jpa.entities.Salle;
import com.example.jpa.entities.Utilisateur;
import com.example.jpa.persistence.JpaContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Service Java SE sans EJB, utilisant la factory singleton de JpaContext.
 * Chaque appel ouvre et ferme son EntityManager; celui-ci n'est jamais partagé.
 */
public final class ServiceGestion implements AutoCloseable {
    private final EntityManagerFactory emf;
    private final boolean ownsFactory;
    private boolean closed;

    /** Les services de l'application réutilisent tous la même factory. */
    public ServiceGestion() {
        emf = JpaContext.getEntityManagerFactory();
        ownsFactory = false;
    }

    /** Factory dédiée pour les tests avec une base et une configuration isolées. */
    public ServiceGestion(Map<String, ?> properties) {
        emf = Persistence.createEntityManagerFactory("gestionPU", properties);
        ownsFactory = true;
    }

    /** Ferme l'EntityManager même si la DAO remonte une erreur après son rollback. */
    private <R> R withManager(Function<EntityManager, R> action) {
        if (closed) throw new IllegalStateException("Le service est fermé");
        EntityManager em = emf.createEntityManager();
        try { return action.apply(em); } finally { em.close(); }
    }
    public void ajouterSalle(Salle salle) { withManager(em -> { new SalleDao(em).create(salle); return null; }); }
    public void ajouterUtilisateur(Utilisateur utilisateur) { withManager(em -> { new UtilisateurDao(em).create(utilisateur); return null; }); }
    public Salle trouverSalle(Long id) { return withManager(em -> new SalleDao(em).find(id)); }
    public Utilisateur trouverUtilisateur(Long id) { return withManager(em -> new UtilisateurDao(em).find(id)); }
    public Salle modifierSalle(Salle salle) { return withManager(em -> new SalleDao(em).update(salle)); }
    public Utilisateur modifierUtilisateur(Utilisateur utilisateur) { return withManager(em -> new UtilisateurDao(em).update(utilisateur)); }
    public void supprimerSalle(Salle salle) { withManager(em -> { new SalleDao(em).delete(salle); return null; }); }
    public void supprimerUtilisateur(Utilisateur utilisateur) { withManager(em -> { new UtilisateurDao(em).delete(utilisateur); return null; }); }
    public List<Salle> listerSalles() { return withManager(em -> new SalleDao(em).findAll()); }
    public List<Utilisateur> listerUtilisateurs() { return withManager(em -> new UtilisateurDao(em).findAll()); }
    /** Nombre de salles, calculé par COUNT en base. */
    public long compterSalles() { return withManager(em -> new SalleDao(em).count()); }
    /** Nombre d'utilisateurs, calculé par COUNT en base. */
    public long compterUtilisateurs() { return withManager(em -> new UtilisateurDao(em).count()); }
    public List<Utilisateur> utilisateursParSalle(String nomSalle) { return withManager(em -> new UtilisateurDao(em).findBySalleNom(nomSalle)); }
    public List<Utilisateur> utilisateursParNom(String fragment) { return withManager(em -> new UtilisateurDao(em).findByNomContains(fragment)); }
    public List<Salle> sallesParCapaciteMin(int min) { return withManager(em -> new SalleDao(em).findByCapaciteMin(min)); }
    public List<Salle> sallesParCapaciteSuperieure(int min) { return withManager(em -> new SalleDao(em).findByCapaciteSuperieure(min)); }
    /** Ferme le service sans fermer la factory partagée des autres services. */
    public void fermer() {
        if (closed) return;
        closed = true;
        if (ownsFactory && emf.isOpen()) emf.close();
    }
    @Override public void close() { fermer(); }
}
