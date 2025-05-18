package org.example.projets2.controller;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.projets2.dao.CreneauDao;
import org.example.projets2.dao.JdbcCreneauDao;
import org.example.projets2.model.Creneau;
import org.example.projets2.util.Session;
import org.example.projets2.model.Utilisateur;
import com.calendarfx.view.CalendarView;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

public class PlanningController implements Initializable {

    @FXML private Button logoutButton;
    // 1) Injection du Label
    @FXML private Label welcomeLabel;

    @FXML
    private CalendarView calendarView;

    private final CreneauDao creneauDao = new JdbcCreneauDao();

    // Référence au calendrier CalendarFX qui contiendra les entrées
    private Calendar userCalendar;


    /**
     * Méthode appelée par JavaFX juste après le chargement du FXML.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1) Afficher d’abord le message de bienvenue
        displayWelcomeUser();

        // 2) Configurer ensuite le CalendarView (création de userCalendar)
        configureCalendarView();

        // 3) Charger les créneaux dans userCalendar
        try {
            loadCreneaux();
        } catch (SQLException e) {
            e.printStackTrace();
            // Tu peux aussi afficher un message à l'utilisateur :
            // welcomeLabel.setText("Erreur chargement planning : " + e.getMessage());
        }

        // 4) Lancer le thread de mise à jour date/heure
        startTimeUpdater();
    }

    private void displayWelcomeUser() {
        Utilisateur current = Session.getCurrentUser();
        if (current != null) {
            String prenom = current.getFirstName();
            String nom    = current.getLastName();
            welcomeLabel.setText(
                    "Planning — Connexion réussie ! Bienvenue " +
                            prenom + " " + nom + " !"
            );
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

    private void configureCalendarView() {
        // 1) Création du calendrier et affectation au champ
        this.userCalendar = new Calendar("Mon planning");
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

    private void loadCreneaux() throws SQLException {
        // Récupérer tous les créneaux
        List<Creneau> tous = creneauDao.findAll();

        // Si tu veux n’afficher que ceux de l’enseignant connecté :
        int userId = Session.getCurrentUser().getId();

        for (Creneau c : tous) {
            // Par exemple : n’afficher que si c.getCours().getEnseignant().getId() == userId
            if (c.getCours().getEnseignant().getId() != userId) {
                continue;
            }

            // Créer l’entrée CalendarFX
            Entry<Creneau> entry = new Entry<>(c.getCours().getNom());
            // Intervalle du même jour, de début à fin
            LocalDate date = c.getDate();
            LocalTime d    = c.getDebut();
            LocalTime f    = c.getFin();
            entry.setInterval(
                    LocalDateTime.of(date, d),
                    LocalDateTime.of(date, f)
            );
            // On peut stocker l’objet métier pour y accéder plus tard
            entry.setUserObject(c);

            // Ajouter l’entrée au calendrier
            userCalendar.addEntry(entry);
        }
    }
}