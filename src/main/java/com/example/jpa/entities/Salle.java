package com.example.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/** Salle identifiée par un nom unique, avec une capacité strictement positive. */
@Entity
public class Salle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    @Column(nullable = false)
    private int capacite;

    /** Constructeur sans argument utilisé par JPA. */
    public Salle() {
    }

    public Salle(String nom, int capacite) {
        setNom(nom);
        setCapacite(capacite);
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom de salle est obligatoire");
        }
        this.nom = nom;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        if (capacite <= 0) {
            throw new IllegalArgumentException("La capacité doit être strictement positive");
        }
        this.capacite = capacite;
    }

    @Override
    public String toString() {
        return "Salle [id=" + id + ", nom=" + nom + ", capacite=" + capacite + "]";
    }
}
