package org.example.projets2.model;

public class Cours {
    private int id;
    private String nom;
    private Utilisateur enseignant;
    private int duree; // en minutes

    public Cours() {}

    public Cours(String nom, Utilisateur enseignant, int duree) {
        this.nom = nom;
        this.enseignant = enseignant;
        this.duree = duree;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public Utilisateur getEnseignant() { return enseignant; }
    public void setEnseignant(Utilisateur enseignant) { this.enseignant = enseignant; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }
}