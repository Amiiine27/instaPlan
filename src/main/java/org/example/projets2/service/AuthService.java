package org.example.projets2.service;

import org.example.projets2.dao.UtilisateurDao;
import org.example.projets2.dao.JdbcUtilisateurDao;
import org.example.projets2.exception.AuthenticationException;
import org.example.projets2.model.Utilisateur;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

/**
 * Service dédié à l'authentification des utilisateurs.
 */
public class AuthService {


    private final UtilisateurDao utilisateurDao;

    /**
     * Constructeur par défaut : utilise l'implémentation JDBC.
     * On pourrait surcharger ce constructeur pour injecter un autre DAO (mock, JPA, etc.).
     */
    public AuthService() {
        this.utilisateurDao = new JdbcUtilisateurDao();
    }

    /**
     * Tente de connecter un utilisateur en vérifiant email et mot de passe.
     *
     * @param email    L'adresse email de l'utilisateur (identifiant).
     * @param password Le mot de passe saisi en clair.
     * @return L'objet Utilisateur chargé depuis la base si tout est correct.
     * @throws AuthenticationException si un des contrôles d'authentification échoue.
     */
    public Utilisateur login(String email, String password) throws AuthenticationException {
        // 1. Validation des paramètres
        if (email == null || email.isBlank()) {
            throw new AuthenticationException("Email obligatoire");
        }
        if (password == null || password.isBlank()) {
            throw new AuthenticationException("Mot de passe obligatoire");
        }

        try {
            // 2. Recherche de l'utilisateur par email
            Utilisateur u = utilisateurDao.findByEmail(email);
            if (u == null) {
                // pas de compte trouvé
                throw new AuthenticationException("Aucun compte trouvé pour cet email");
            }

            // 3. Vérification du mot de passe
            // (à remplacer par un hachage sécurisé en production)
            if (!BCrypt.checkpw(password, u.getPassword())) {
                throw new AuthenticationException("Mot de passe incorrect");
            }

            // 4. Authentification réussie
            return u;

        } catch (SQLException e) {
            // 5. Problème technique (base inaccessible, etc.)
            throw new AuthenticationException("Erreur d'accès à la base de données", e);
        }
    }
}