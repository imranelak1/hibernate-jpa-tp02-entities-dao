package com.example.jpa.dao;

import com.example.jpa.entities.Salle;
import javax.persistence.EntityManager;
import java.util.List;

/** CRUD des salles et recherches par capacité avec paramètres JPQL. */
public final class SalleDao extends GenericDao<Salle> {
    public SalleDao(EntityManager em) { super(Salle.class, em); }
    /** Inclut les salles dont la capacité est égale à la borne. */
    public List<Salle> findByCapaciteMin(int capaciteMin) {
        return em.createQuery("SELECT s FROM Salle s WHERE s.capacite >= :capaciteMin ORDER BY s.capacite", Salle.class)
                .setParameter("capaciteMin", capaciteMin).getResultList();
    }
    /** Recherche avec une borne strictement supérieure. */
    public List<Salle> findByCapaciteSuperieure(int minCapacite) {
        return em.createQuery("SELECT s FROM Salle s WHERE s.capacite > :minCapacite ORDER BY s.capacite", Salle.class)
                .setParameter("minCapacite", minCapacite).getResultList();
    }
}
