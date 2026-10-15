package com.example.jpa.dao;

import com.example.jpa.entities.Utilisateur;
import javax.persistence.EntityManager;
import java.util.List;

/** CRUD des utilisateurs et recherches par salle ou fragment de nom. */
public final class UtilisateurDao extends GenericDao<Utilisateur> {
    public UtilisateurDao(EntityManager em) {
        super(Utilisateur.class, em);
    }

    /** Filtre les utilisateurs par le nom de leur salle. */
    public List<Utilisateur> findBySalleNom(String nomSalle) {
        return em.createQuery("SELECT u FROM Utilisateur u WHERE u.salle.nom = :nomSalle ORDER BY u.id", Utilisateur.class)
                .setParameter("nomSalle", nomSalle)
                .getResultList();
    }

    /** Recherche littérale: les caractères % et _ ne deviennent pas des jokers. */
    public List<Utilisateur> findByNomContains(String fragment) {
        if (fragment == null) {
            throw new IllegalArgumentException("Le fragment ne peut pas être null");
        }
        String literal = fragment.replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return em.createQuery("SELECT u FROM Utilisateur u WHERE u.nom LIKE :pattern ESCAPE '!' ORDER BY u.id", Utilisateur.class)
                .setParameter("pattern", "%" + literal + "%")
                .getResultList();
    }
}
