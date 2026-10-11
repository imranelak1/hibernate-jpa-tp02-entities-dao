package com.example.jpa.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenericDao<T> {
    private static final Logger LOG = Logger.getLogger(GenericDao.class.getName());
    private final Class<T> entityClass;
    protected final EntityManager em;
    public GenericDao(Class<T> entityClass, EntityManager em) { this.entityClass = entityClass; this.em = em; }
    public void create(T entity) { write(() -> { em.persist(entity); return null; }); }
    public T find(Object id) { return em.find(entityClass, id); }
    public T update(T entity) { return write(() -> em.merge(entity)); }
    public void delete(T entity) {
        write(() -> { em.remove(em.contains(entity) ? entity : em.merge(entity)); return null; });
    }
    public List<T> findAll() {
        return em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e ORDER BY e.id", entityClass).getResultList();
    }
    private <R> R write(Supplier<R> action) {
        EntityTransaction tx = em.getTransaction();
        if (tx.isActive()) throw new IllegalStateException("La DAO attend une unite de travail sans transaction active");
        try {
            tx.begin(); R result = action.get(); tx.commit(); return result;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            em.clear();
            LOG.log(Level.WARNING, "Transaction annulee pour {0}", entityClass.getSimpleName());
            throw ex;
        }
    }
}
