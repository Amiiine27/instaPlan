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
    private Role role;

    /** Constructeur vide nécessaire au DAO. */
    public Utilisateur() { }

    /** Constructeur pour créer un nouvel utilisateur (avant insertion). */
    public Utilisateur(String lastName, String firstName, String password) {
        this.lastName  = lastName;
        this.firstName = firstName;
        this.email     = (firstName + "." +  lastName + "@example.com").toLowerCase();
        this.password  = password;
        this.role = Role.ETUDIANT;  // Par défaut
    }

    /** Constructeur avec rôle spécifié */
    public Utilisateur(String lastName, String firstName, String password, Role role) {
        this(lastName, firstName, password);
        this.role = role;
    }

    /** Constructeur pour créer un nouvel utilisateur (avant insertion). */
    public Utilisateur(String lastName, String firstName) {
        this.lastName  = lastName;
        this.firstName = firstName;
        this.email     = (firstName + "." +  lastName + "@example.com").toLowerCase();
        this.role = Role.ETUDIANT;  // Par défaut
    }

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

    public Role getRole() {
        return role;
    }
    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isEnseignant() {
        return role == Role.ENSEIGNANT;
    }

    public boolean isEtudiant() {
        return role == Role.ETUDIANT;
    }
}