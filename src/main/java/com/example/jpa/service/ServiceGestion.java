package com.example.jpa.service;

import com.example.jpa.dao.SalleDao;
import com.example.jpa.dao.UtilisateurDao;
import com.example.jpa.entities.Salle;
import com.example.jpa.entities.Utilisateur;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Java SE sans EJB : factory partagee et EntityManager de courte duree. */
public final class ServiceGestion implements AutoCloseable {
    private final EntityManagerFactory emf;
    public ServiceGestion() { this(Map.of()); }
    public ServiceGestion(Map<String, ?> properties) { emf = Persistence.createEntityManagerFactory("gestionPU", properties); }
    private <R> R withManager(Function<EntityManager, R> action) {
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
    public List<Utilisateur> utilisateursParSalle(String nomSalle) { return withManager(em -> new UtilisateurDao(em).findBySalleNom(nomSalle)); }
    public List<Utilisateur> utilisateursParNom(String fragment) { return withManager(em -> new UtilisateurDao(em).findByNomContains(fragment)); }
    public List<Salle> sallesParCapaciteMin(int min) { return withManager(em -> new SalleDao(em).findByCapaciteMin(min)); }
    public List<Salle> sallesParCapaciteSuperieure(int min) { return withManager(em -> new SalleDao(em).findByCapaciteSuperieure(min)); }
    public void fermer() { if (emf.isOpen()) emf.close(); }
    @Override public void close() { fermer(); }
}
