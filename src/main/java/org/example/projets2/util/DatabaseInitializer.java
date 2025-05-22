package org.example.projets2.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Initialise la base de données en exécutant tous les scripts SQL
 * placés dans src/main/resources/db/.
 */
public class DatabaseInitializer {

    // Liste des scripts à exécuter, dans l'ordre
    private static final List<String> SCRIPTS = Arrays.asList(
            "/db/init_utilisateur.sql",
            "/db/init_cours.sql",
            "/db/init_salle.sql",
            "/db/init_creneau.sql",
            "/db/init_notification.sql",
            "/db/init_groupe.sql",
            "/db/init_groupe_etudiant.sql",
            "/db/init_groupe_creneau.sql"
    );

    /**
     * Parcourt chaque script, le lit depuis les ressources et exécute
     * toutes les instructions SQL qu'il contient (séparées par ';').
     */
    public static void initialize() {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String path : SCRIPTS) {
                String sql = readResource(path);
                // On coupe sur ';' pour exécuter chaque DDL séparément
                for (String ddl : sql.split(";")) {
                    String trimmed = ddl.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }

            System.out.println("✅ Base initialisée : tables Utilisateur, Cours, Salle, Creneau créées si besoin.");
        } catch (Exception e) {
            throw new RuntimeException("❌ Impossible d'initialiser la base", e);
        }
    }

    /**
     * Lit le contenu textuel d'un fichier SQL dans /resources/db/.
     *
     * @param resourcePath chemin de la ressource à charger (ex. "/db/init_utilisateur.sql")
     * @return le contenu complet du fichier sous forme de chaîne
     * @throws Exception si la ressource est introuvable ou la lecture échoue
     */
    private static String readResource(String resourcePath) throws Exception {
        InputStream in = DatabaseInitializer.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("Ressource introuvable : " + resourcePath);
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {
            // On reconstitue tout le fichier
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}