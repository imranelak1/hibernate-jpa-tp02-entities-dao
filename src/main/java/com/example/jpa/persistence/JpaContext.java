package com.example.jpa.persistence;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/** Factory unique de l'application, partagée entre les services. */
public final class JpaContext {
    private JpaContext() { }

    // Le holder initialise la factory une seule fois, au premier accès.
    private static final class Holder {
        private static final EntityManagerFactory FACTORY =
                Persistence.createEntityManagerFactory("gestionPU");
    }

    /** La factory est partageable; chaque opération crée son propre EntityManager. */
    public static EntityManagerFactory getEntityManagerFactory() {
        return Holder.FACTORY;
    }

    /** À appeler à l'arrêt de l'application, après la fermeture des services. */
    public static void close() {
        if (Holder.FACTORY.isOpen()) Holder.FACTORY.close();
    }
}
