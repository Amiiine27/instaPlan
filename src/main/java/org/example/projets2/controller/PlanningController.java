package org.example.projets2.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.projets2.util.Session;
import org.example.projets2.model.Utilisateur;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PlanningController implements Initializable {

    @FXML private Button logoutButton;
    // 1) Injection du Label
    @FXML private Label welcomeLabel;

    /**
     * Méthode appelée par JavaFX juste après le chargement du FXML.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 2) Récupérer l'utilisateur de la session
        Utilisateur current = Session.getCurrentUser();
        if (current != null) {
            // 3) Mettre à jour le texte du Label
            String prenom = current.getFirstName();
            String nom    = current.getLastName();
            welcomeLabel.setText(
                    "Planning — Connexion réussie ! Bienvenue " +
                            prenom + " " + nom + " !"
            );
        } else {
            // au cas où : si personne n'est connecté, on affiche un message générique
            welcomeLabel.setText("Planning — Connexion réussie !");
        }
    }

    @FXML
    private void onLogoutClicked() {
        // déjà implémenté.
        Session.clear();
        try {
            Parent loginRoot = FXMLLoader.load(
                    getClass().getResource("/org/example/projets2/views/login-view.fxml")
            );
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(loginRoot, stage.getScene().getWidth(), stage.getScene().getHeight()));
            stage.setTitle("InstaPlan - Connexion");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}