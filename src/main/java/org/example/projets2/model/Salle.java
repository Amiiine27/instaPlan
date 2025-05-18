package org.example.projets2.model;

public class Salle {
    private int id;
    private String nom;
    private int capacite;
    private String equipements; // ex. "Projecteur,Climatisation"

    public Salle() {}

    public Salle(String nom, int capacite, String equipements) {
        this.nom = nom;
        this.capacite = capacite;
        this.equipements = equipements;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public String getEquipements() { return equipements; }
    public void setEquipements(String equipements) { this.equipements = equipements; }
}