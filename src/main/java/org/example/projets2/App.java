package org.example.projets2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.projets2.dao.JdbcUtilisateurDao;
import org.example.projets2.dao.UtilisateurDao;
import org.example.projets2.model.Utilisateur;

import java.sql.SQLException;

/**
 * Classe principale de l'application InstaPlan.
 * Elle initialise JavaFX, charge la vue de login et affiche la fenêtre principale.
 */
public class App extends Application {

    /**
     * Point d'entrée JavaFX.
     * @param primaryStage la fenêtre principale fournie par le runtime JavaFX
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(
                getClass().getResource("/org/example/projets2/views/login-view.fxml")
        );
        Scene scene = new Scene(root, 1500, 750);
        primaryStage.setTitle("InstaPlan");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Méthode main standard en Java.
     * Elle déclenche le cycle de vie JavaFX.
     */
    public static void main(String[] args) throws SQLException {
        launch(args);
        UtilisateurDao dao = new JdbcUtilisateurDao();
        Utilisateur u = new Utilisateur("Dupont", "Alice", "alice@example.com", "mdp");
        dao.create(u);
        System.out.println("Nouvel ID = " + u.getId());
        Utilisateur lu = dao.findByEmail("alice@example.com");
        System.out.println("Lu en base : " + lu.getFirstName() + " " + lu.getLastName());
    }
}