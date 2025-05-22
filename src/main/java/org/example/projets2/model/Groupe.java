package org.example.projets2.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un groupe d'étudiants.
 */
public class Groupe {
    private int id;
    private String nom;
    private String description;
    private List<Utilisateur> etudiants;

    /**
     * Constructeur par défaut.
     */
    public Groupe() {
        this.etudiants = new ArrayList<>();
    }

    /**
     * Constructeur avec paramètres.
     */
    public Groupe(String nom, String description) {
        this();
        this.nom = nom;
        this.description = description;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Utilisateur> getEtudiants() {
        return etudiants;
    }

    public void setEtudiants(List<Utilisateur> etudiants) {
        this.etudiants = etudiants;
    }

    /**
     * Ajoute un étudiant au groupe s'il n'en fait pas déjà partie.
     */
    public void addEtudiant(Utilisateur etudiant) {
        if (!etudiants.contains(etudiant)) {
            etudiants.add(etudiant);
        }
    }

    /**
     * Retire un étudiant du groupe.
     */
    public void removeEtudiant(Utilisateur etudiant) {
        etudiants.remove(etudiant);
    }

    /**
     * Vérifie si un étudiant fait partie du groupe.
     */
    public boolean containsEtudiant(Utilisateur etudiant) {
        return etudiants.contains(etudiant);
    }

    /**
     * Vérifie si un étudiant fait partie du groupe par son ID.
     */
    public boolean containsEtudiantById(int etudiantId) {
        return etudiants.stream()
                .anyMatch(e -> e.getId() == etudiantId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Groupe groupe = (Groupe) o;
        return id == groupe.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return nom;
    }
}