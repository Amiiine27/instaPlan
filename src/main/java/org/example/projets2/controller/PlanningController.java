package org.example.projets2.controller;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import javafx.application.Platform;
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
import com.calendarfx.view.CalendarView;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class PlanningController implements Initializable {

    @FXML private Button logoutButton;
    // 1) Injection du Label
    @FXML private Label welcomeLabel;

    @FXML
    private CalendarView calendarView;


    /**
     * Méthode appelée par JavaFX juste après le chargement du FXML.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 2) Récupérer l'utilisateur de la session
        Utilisateur current = Session.getCurrentUser();

        // 3) Mettre à jour le texte du Label
        String prenom = current.getFirstName();
        String nom    = current.getLastName();
        welcomeLabel.setText(
                "Planning — Connexion réussie ! Bienvenue " +
                        prenom + " " + nom + " !"
        );

        configureCalendarView();
        startTimeUpdater();

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

    private void configureCalendarView() {
        // 1) Création de calendriers
        Calendar userCalendar = new Calendar("Mon planning");
        userCalendar.setStyle(Calendar.Style.STYLE1);

        Calendar meetings = new Calendar("Réunions");
        meetings.setStyle(Calendar.Style.STYLE2);

        // 2) Création de la source et association
        CalendarSource mySource = new CalendarSource("Mes calendriers");
        mySource.getCalendars().addAll(userCalendar, meetings);

        // 3) Ajout au CalendarView
        calendarView.getCalendarSources().addAll(mySource);

        // 4) Place l'heure demandée sur l'heure actuelle
        calendarView.setRequestedTime(LocalTime.now());
    }

    private void startTimeUpdater() {
        Thread updateThread = new Thread(() -> {
            while (true) {
                Platform.runLater(() -> {
                    calendarView.setToday(LocalDate.now());
                    calendarView.setTime(LocalTime.now());
                });
                try {
                    Thread.sleep(10_000); // toutes les 10 secondes
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "Calendar: Update Time Thread");

        updateThread.setDaemon(true);
        updateThread.start();
    }
}