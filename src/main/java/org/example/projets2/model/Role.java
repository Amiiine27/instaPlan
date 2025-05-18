package org.example.projets2.model;

/**
 * Représente les différents rôles d'utilisateurs dans le système.
 */
public enum Role {
    ETUDIANT("Étudiant"),
    ENSEIGNANT("Enseignant"),
    ADMIN("Administrateur");

    private final String libelle;

    Role(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    /**
     * Convertit une chaîne en Role.
     * Utile pour convertir les valeurs de la base de données en enum.
     */
    public static Role fromString(String roleStr) {
        if (roleStr == null) {
            return null;
        }

        try {
            return Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Si le rôle n'existe pas, on retourne null
            return null;
        }
    }

    @Override
    public String toString() {
        return libelle;
    }
}