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
        // 1) Afficher d'abord le message de bienvenue
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
            String nom = current.getLastName();
            String roleStr = "";

            if (current.isAdmin()) {
                roleStr = " (Administrateur)";
            } else if (current.isEnseignant()) {
                roleStr = " (Enseignant)";
            } else if (current.isEtudiant()) {
                roleStr = " (Étudiant)";
            }

            welcomeLabel.setText(
                    "Planning — Bienvenue " + prenom + " " + nom + roleStr
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
        System.out.println("Nombre total de créneaux trouvés: " + tous.size());

        // Récupérer l'utilisateur connecté
        Utilisateur currentUser = Session.getCurrentUser();
        System.out.println("Utilisateur connecté: " + currentUser.getFirstName() + " " + currentUser.getLastName());
        System.out.println("ID utilisateur: " + currentUser.getId());
        System.out.println("Rôle utilisateur: " + currentUser.getRole());

        int displayedCount = 0;
        for (Creneau c : tous) {
            System.out.println("\nAnalyse créneau #" + c.getId());
            System.out.println("  Date du créneau: " + c.getDate());
            System.out.println("  Heure de début: " + c.getDebut());
            System.out.println("  Heure de fin: " + c.getFin());
            System.out.println("  Cours: " + c.getCours().getNom());
            System.out.println("  Enseignant du cours: " + c.getCours().getEnseignant().getFirstName() + " " + c.getCours().getEnseignant().getLastName());
            System.out.println("  ID enseignant: " + c.getCours().getEnseignant().getId());

            // Logique de filtrage selon le rôle
            boolean shouldDisplay = false;

            if (currentUser.isAdmin()) {
                System.out.println("  -> Utilisateur est ADMIN, affichage permis");
                shouldDisplay = true;
            } else if (currentUser.isEnseignant()) {
                shouldDisplay = c.getCours().getEnseignant().getId() == currentUser.getId();
                System.out.println("  -> Utilisateur est ENSEIGNANT, affichage " + (shouldDisplay ? "permis" : "refusé"));
            } else if (currentUser.isEtudiant()) {
                System.out.println("  -> Utilisateur est ETUDIANT, affichage permis");
                shouldDisplay = true;
            } else {
                System.out.println("  -> Rôle non reconnu: " + currentUser.getRole());
            }

            if (!shouldDisplay) {
                System.out.println("  => Créneau ignoré");
                continue;
            }

            System.out.println("  => Ajout du créneau au calendrier");
            displayedCount++;

            // Créer l'entrée CalendarFX
            Entry<Creneau> entry = new Entry<>(c.getCours().getNom());

            // Ajouter un préfixe au titre selon le rôle
            if (currentUser.isAdmin()) {
                entry.setTitle("[ADMIN] " + c.getCours().getNom());
            } else if (currentUser.isEnseignant()) {
                entry.setTitle("[PROF] " + c.getCours().getNom());
            }

            // Intervalle du même jour, de début à fin
            LocalDate date = c.getDate();
            LocalTime d = c.getDebut();
            LocalTime f = c.getFin();

            LocalDateTime startDateTime = LocalDateTime.of(date, d);
            LocalDateTime endDateTime = LocalDateTime.of(date, f);

            System.out.println("  Setting event interval: " + startDateTime + " to " + endDateTime);

            entry.setInterval(startDateTime, endDateTime);

            // Ajouter la salle comme localisation
            entry.setLocation(c.getSalle().getNom());

            // On peut stocker l'objet métier pour y accéder plus tard
            entry.setUserObject(c);

            // Ajouter l'entrée au calendrier
            userCalendar.addEntry(entry);
            System.out.println("  Entry added to calendar: " + entry.getTitle());
        }

        System.out.println("\nTotal de créneaux affichés: " + displayedCount);

        // Vérifier si le calendrier contient des entrées
        System.out.println("Nombre d'entrées dans le calendrier: " + userCalendar.findEntries(String.valueOf(LocalDate.now())).size());

        // Si aucun créneau n'est affiché, ajoutons un événement de test
        if (displayedCount == 0) {
            System.out.println("Aucun créneau trouvé - Ajout d'un événement de test");
            Entry<String> testEntry = new Entry<>("ÉVÉNEMENT TEST");
            testEntry.setInterval(
                    LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0)),
                    LocalDateTime.of(LocalDate.now(), LocalTime.of(13, 0))
            );
            userCalendar.addEntry(testEntry);
            System.out.println("Événement de test ajouté au calendrier");
        }
    }
}