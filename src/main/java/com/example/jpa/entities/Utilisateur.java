package com.example.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Utilisateur {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nom;
    @Column(nullable = false, unique = true)
    private String email;
    @ManyToOne @JoinColumn(name = "salle_id")
    private Salle salle;
    public Utilisateur() { }
    public Utilisateur(String nom, String email, Salle salle) { setNom(nom); setEmail(email); setSalle(salle); }
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String nom) {
        if (nom == null || nom.isBlank()) throw new IllegalArgumentException("Le nom est obligatoire");
        this.nom = nom;
    }
    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("L'email est obligatoire");
        this.email = email;
    }
    public Salle getSalle() { return salle; }
    public void setSalle(Salle salle) { this.salle = salle; }
    @Override public String toString() {
        return "Utilisateur [id=" + id + ", nom=" + nom + ", email=" + email + ", salle=" + (salle == null ? "aucune" : salle.getNom()) + "]";
    }
}
