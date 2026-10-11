package com.example.jpa.dao;

import com.example.jpa.entities.Utilisateur;
import javax.persistence.EntityManager;
import java.util.List;

public final class UtilisateurDao extends GenericDao<Utilisateur> {
    public UtilisateurDao(EntityManager em) { super(Utilisateur.class, em); }
    public List<Utilisateur> findBySalleNom(String nomSalle) {
        return em.createQuery("SELECT u FROM Utilisateur u WHERE u.salle.nom = :nomSalle ORDER BY u.id", Utilisateur.class)
                .setParameter("nomSalle", nomSalle).getResultList();
    }
    public List<Utilisateur> findByNomContains(String fragment) {
        if (fragment == null) throw new IllegalArgumentException("Le fragment ne peut pas etre null");
        String literal = fragment.replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return em.createQuery("SELECT u FROM Utilisateur u WHERE u.nom LIKE :pattern ESCAPE '!' ORDER BY u.id", Utilisateur.class)
                .setParameter("pattern", "%" + literal + "%").getResultList();
    }
}
