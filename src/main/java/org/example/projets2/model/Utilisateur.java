package org.example.projets2.model;

/**
 * Représente un utilisateur tel que défini dans la table SQL Utilisateur.
 */
public class Utilisateur {
    private int id;
    private String lastName;
    private String firstName;
    private String email;
    private String password;

    /** Constructeur vide nécessaire au DAO. */
    public Utilisateur() { }

    /** Constructeur pour créer un nouvel utilisateur (avant insertion). */
    public Utilisateur(String lastName, String firstName, String email, String password) {
        this.lastName  = lastName;
        this.firstName = firstName;
        this.email     = email;
        this.password  = password;
    }

    // Getters et setters JavaBean

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}