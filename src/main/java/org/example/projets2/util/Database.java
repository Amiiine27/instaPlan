package org.example.projets2.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fournit une connexion JDBC vers la base SQLite de l’application.
 */
public class Database {

    /** JDBC URL pointant vers le fichier SQLite dans le dossier racine du projet. */
    private static final String URL = "jdbc:sqlite:projets2.db";

    /**
     * Ouvre et retourne une connexion.
     * @throws SQLException si la connexion échoue.
     */
    public static Connection getConnection() throws SQLException {
        String url = URL; // "jdbc:sqlite:projets2.db"
        // Pour voir où le fichier est créé / ouvert :
        System.out.println("SQLite DB path: " +
                new java.io.File("projets2.db").getAbsolutePath()
        );
        return DriverManager.getConnection(URL);
    }
}