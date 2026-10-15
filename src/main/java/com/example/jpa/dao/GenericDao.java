package com.example.jpa.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Opérations CRUD communes aux entités du TP.
 * L'EntityManager est fourni par le service et reste propre à une opération.
 * Les écritures passent toutes par {@link #write(Supplier)} pour gérer le rollback.
 * @param <T> type de l'entité manipulée
 */
public class GenericDao<T> {
    private static final Logger LOG = Logger.getLogger(GenericDao.class.getName());

    private final Class<T> entityClass;
    protected final EntityManager em;

    public GenericDao(Class<T> entityClass, EntityManager em) {
        this.entityClass = entityClass;
        this.em = em;
    }

    /** Enregistre une entité et annule la transaction si une contrainte échoue. */
    public void create(T entity) {
        write(() -> {
            em.persist(entity);
            return null;
        });
    }

    /** Recherche par identifiant, ou renvoie null si l'entité n'existe pas. */
    public T find(Object id) {
        return em.find(entityClass, id);
    }

    /** Met à jour une entité, même détachée, dans une transaction. */
    public T update(T entity) {
        return write(() -> em.merge(entity));
    }

    /** Supprime une entité et conserve les données si la suppression échoue. */
    public void delete(T entity) {
        write(() -> {
            em.remove(em.contains(entity) ? entity : em.merge(entity));
            return null;
        });
    }

    /** Liste toutes les entités, dans l'ordre de leurs identifiants. */
    public List<T> findAll() {
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e ORDER BY e.id";
        return em.createQuery(jpql, entityClass).getResultList();
    }

    /** Compte les lignes en base sans charger toute la liste en mémoire. */
    public long count() {
        String jpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e";
        return em.createQuery(jpql, Long.class).getSingleResult();
    }

    /** Une même gestion begin/commit/rollback pour create, update et delete. */
    private <R> R write(Supplier<R> action) {
        EntityTransaction tx = em.getTransaction();
        if (tx.isActive()) {
            throw new IllegalStateException("La DAO attend une unité de travail sans transaction active");
        }

        try {
            tx.begin();
            R result = action.get();
            // Une contrainte peut être vérifiée pendant persist, ou seulement au commit.
            tx.commit();
            return result;
        } catch (RuntimeException ex) {
            try {
                if (tx.isActive()) {
                    tx.rollback();
                }
            } catch (RuntimeException rollbackFailure) {
                ex.addSuppressed(rollbackFailure);
            }
            // Les objets modifiés en mémoire ne doivent pas être réutilisés après l'échec.
            try {
                em.clear();
            } catch (RuntimeException clearFailure) {
                ex.addSuppressed(clearFailure);
            }
            LOG.log(Level.WARNING, "Transaction annulée pour {0}", entityClass.getSimpleName());
            throw ex;
        }
    }
}
