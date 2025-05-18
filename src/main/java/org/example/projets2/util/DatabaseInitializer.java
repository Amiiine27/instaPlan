package org.example.projets2.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    /**
     * Exécute le script SQL situé dans /db/init_utilisateur.sql
     * pour s'assurer que la table Utilisateur existe.
     */
    public static void initialize() {
        try (Connection c = Database.getConnection();
             Statement stmt = c.createStatement()) {

            // Lire le contenu du script
            String sql = new String(
                    Files.readAllBytes(Paths.get(
                            DatabaseInitializer.class
                                    .getResource("/db/init_utilisateur.sql").toURI()
                    )),
                    StandardCharsets.UTF_8
            );

            // Exécuter chaque commande DDL du script
            for (String ddl : sql.split(";")) {
                if (!ddl.trim().isEmpty()) {
                    stmt.execute(ddl);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Impossible d'initialiser la base", e);
        }
    }
}