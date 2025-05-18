package org.example.projets2;

import org.example.projets2.dao.JdbcUtilisateurDao;
import org.example.projets2.dao.UtilisateurDao;
import org.example.projets2.exception.AuthenticationException;
import org.example.projets2.model.Utilisateur;
import org.example.projets2.service.AuthService;
import org.example.projets2.util.DatabaseInitializer;

import java.sql.SQLException;

public class MainTestAll {

    public static void main(String[] args) {
        // 1. Initialiser la base (crée la table Utilisateur si nécessaire)
        System.out.println("→ Initialisation de la base…");
        DatabaseInitializer.initialize();

        // 2. Instancier le DAO
        UtilisateurDao dao = new JdbcUtilisateurDao();

        // 3. Créer un utilisateur de test
        Utilisateur u = new Utilisateur(
                "Martin",
                "Paul",
                "paul.martin@example.com",
                "secret123"
        );
        try {
            dao.create(u);
            System.out.println("👤 Utilisateur créé avec ID = " + u.getId());
        } catch (SQLException e) {
            System.err.println("❌ Erreur à l'insertion : " + e.getMessage());
        }

        // 4. Lire l'utilisateur par email
        try {
            Utilisateur lu = dao.findByEmail("paul.martin@example.com");
            if (lu != null) {
                System.out.printf("🔍 Trouvé par email : ID=%d, %s %s, hash=%s%n",
                        lu.getId(), lu.getFirstName(), lu.getLastName(), lu.getPassword());
            } else {
                System.err.println("❌ Utilisateur non trouvé par email !");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur à la lecture par email : " + e.getMessage());
        }

        // 5. Lire l'utilisateur par ID
        try {
            Utilisateur lu2 = dao.findById(u.getId());
            if (lu2 != null) {
                System.out.printf("🔍 Trouvé par ID    : ID=%d, %s %s, hash=%s%n",
                        lu2.getId(), lu2.getFirstName(), lu2.getLastName(), lu2.getPassword());
            } else {
                System.err.println("❌ Utilisateur non trouvé par ID !");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur à la lecture par ID : " + e.getMessage());
        }

        // 6. Tester AuthService
        AuthService authService = new AuthService();

        // 6.a Cas réussi
        try {
            Utilisateur connecté = authService.login("paul.martin@example.com", "secret123");
            System.out.println("✅ Auth réussi : " + connecté.getFirstName() + " " + connecté.getLastName());
        } catch (AuthenticationException ex) {
            System.err.println("❌ Auth failed: " + ex.getMessage());
        }

        // 6.b Mot de passe incorrect
        try {
            authService.login("paul.martin@example.com", "mauvaisMdp");
            System.err.println("❌ Devrait avoir échoué avec mauvais mot de passe !");
        } catch (AuthenticationException ex) {
            System.out.println("🔒 Auth correct refusal: " + ex.getMessage());
        }

        // 6.c Email inconnu
        try {
            authService.login("inconnu@example.com", "quelqueMdp");
            System.err.println("❌ Devrait avoir échoué avec email inconnu !");
        } catch (AuthenticationException ex) {
            System.out.println("🔒 Auth correct refusal: " + ex.getMessage());
        }
    }
}