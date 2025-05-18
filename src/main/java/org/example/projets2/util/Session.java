package org.example.projets2.util;

import org.example.projets2.model.Utilisateur;

/**
 * Stocke l'utilisateur actuellement authentifié pour toute la durée de l'application.
 * Implémentation très simple basée sur une variable statique en mémoire.
 */
public class Session {

    // L'utilisateur connecté, ou null s'il n'y en a pas
    private static Utilisateur currentUser = null;

    /**
     * Récupère l'utilisateur actuellement connecté.
     * @return l'objet Utilisateur, ou null si personne n'est connecté.
     */
    public static Utilisateur getCurrentUser() {
        return currentUser;
    }

    /**
     * Définit l'utilisateur connecté.
     * @param user l'instance Utilisateur provenant d'AuthService.login()
     */
    public static void setCurrentUser(Utilisateur user) {
        currentUser = user;
    }

    /**
     * Fournit un indicateur simple sur l'état de connexion.
     * @return true si un utilisateur est connecté (currentUser != null).
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Déconnecte l'utilisateur courant (reset de la session).
     */
    public static void clear() {
        currentUser = null;
    }
}