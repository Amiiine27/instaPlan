package org.example.projets2.model;

import java.time.LocalDateTime;

/**
 * Représente une notification dans le système.
 */
public class Notification {

    public enum Type {
        INFO,           // Information générale
        CREATION,       // Création d'un créneau
        MODIFICATION,   // Modification d'un créneau
        ANNULATION,     // Annulation d'un créneau
        CONFLIT         // Conflit détecté
    }

    private int id;
    private String titre;
    private String message;
    private Type type;
    private LocalDateTime dateCreation;
    private boolean lue;
    private Utilisateur destinataire;
    private Creneau creneau;  // Créneau concerné (optionnel)

    /**
     * Constructeur vide pour le DAO.
     */
    public Notification() {
        this.dateCreation = LocalDateTime.now();
        this.lue = false;
    }

    /**
     * Constructeur pour une notification liée à un créneau.
     */
    public Notification(String titre, String message, Type type, Utilisateur destinataire, Creneau creneau) {
        this();
        this.titre = titre;
        this.message = message;
        this.type = type;
        this.destinataire = destinataire;
        this.creneau = creneau;
    }

    /**
     * Constructeur pour une notification générale.
     */
    public Notification(String titre, String message, Type type, Utilisateur destinataire) {
        this(titre, message, type, destinataire, null);
    }

    // Getters et Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public boolean isLue() {
        return lue;
    }

    public void setLue(boolean lue) {
        this.lue = lue;
    }

    public Utilisateur getDestinataire() {
        return destinataire;
    }

    public void setDestinataire(Utilisateur destinataire) {
        this.destinataire = destinataire;
    }

    public Creneau getCreneau() {
        return creneau;
    }

    public void setCreneau(Creneau creneau) {
        this.creneau = creneau;
    }
}