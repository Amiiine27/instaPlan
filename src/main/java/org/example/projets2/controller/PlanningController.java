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

    @FXML
    private Button manageSallesButton;

    // Référence au calendrier CalendarFX qui contiendra les entrées
    private Calendar userCalendar;
    @FXML private Button manageCoursButton;

    @FXML private Button manageCreneauxButton;

    @FXML private Button notificationsButton;

    /**
     * Méthode appelée par JavaFX juste après le chargement du FXML.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1) Afficher d'abord le message de bienvenue
        displayWelcomeUser();

        // Vérifier si l'utilisateur est admin pour afficher le bouton de gestion des salles
        Utilisateur currentUser = Session.getCurrentUser();
        if (currentUser != null && currentUser.isAdmin()) {
            manageSallesButton.setVisible(true);
            manageCoursButton.setVisible(true);
            manageCreneauxButton.setVisible(true);
        }

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
        // Création du calendrier principal
        this.userCalendar = new Calendar("Mon planning");
        userCalendar.setStyle(Calendar.Style.STYLE1);

        // Création de la source et association
        CalendarSource mySource = new CalendarSource("Mes calendriers");
        mySource.getCalendars().add(userCalendar);

        // Ajout au CalendarView
        calendarView.getCalendarSources().clear();
        calendarView.getCalendarSources().add(mySource);

        // Configuration de l'affichage du calendrier

        // 1. Définir la date sur aujourd'hui
        LocalDate today = LocalDate.now();
        calendarView.setToday(today);
        calendarView.setDate(today);

        // 2. Afficher la vue semaine par défaut pour voir plus de créneaux
        calendarView.showWeekPage();

        // 3. Configuration des options d'affichage disponibles
        // Remarque: certaines méthodes peuvent ne pas être disponibles selon la version de CalendarFX
        try {
            // Essayer de configurer les options si disponibles
            calendarView.setShowAddCalendarButton(false);
        } catch (Exception e) {
            System.out.println("Note: Option setShowAddCalendarButton non disponible");
        }

        try {
            calendarView.setEnableTimeZoneSupport(false);
        } catch (Exception e) {
            System.out.println("Note: Option setEnableTimeZoneSupport non disponible");
        }

        // 4. Configuration du temps visible (alternative)
        // Au lieu d'utiliser setEarliestTime et setLatestTime, on peut utiliser:
        calendarView.setRequestedTime(LocalTime.of(9, 0));  // Heure initiale affichée
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

            // Utiliser la date réelle du créneau, quelle qu'elle soit
            LocalDate date = c.getDate();
            LocalTime d = c.getDebut();
            LocalTime f = c.getFin();

            LocalDateTime startDateTime = LocalDateTime.of(date, d);
            LocalDateTime endDateTime = LocalDateTime.of(date, f);

            System.out.println("  Setting event interval: " + startDateTime + " to " + endDateTime);

            entry.setInterval(startDateTime, endDateTime);

            // Ajouter des informations supplémentaires
            entry.setLocation(c.getSalle().getNom());

            // On peut stocker l'objet métier pour y accéder plus tard
            entry.setUserObject(c);

            // Ajouter l'entrée au calendrier
            userCalendar.addEntry(entry);
            System.out.println("  Entry added to calendar: " + entry.getTitle() + " for date " + date);
        }

        System.out.println("\nTotal de créneaux affichés: " + displayedCount);
    }

    @FXML
    private void onManageSallesClicked() {
        try {
            // Charger la vue de gestion des salles
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/projets2/views/salles-view.fxml"));

            // Créer une nouvelle scène avec cette vue
            Scene scene = new Scene(root, 1500, 750);

            // Obtenir la fenêtre actuelle et changer sa scène
            Stage stage = (Stage) manageSallesButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("InstaPlan - Gestion des Salles");

        } catch (IOException e) {
            e.printStackTrace();
            welcomeLabel.setText("Erreur lors du chargement de la gestion des salles : " + e.getMessage());
        }
    }

    @FXML
    private void onManageCoursClicked() {
        try {
            // Charger la vue de gestion des cours
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/projets2/views/cours-view.fxml"));

            // Créer une nouvelle scène avec cette vue
            Scene scene = new Scene(root, 1500, 750);

            // Obtenir la fenêtre actuelle et changer sa scène
            Stage stage = (Stage) manageCoursButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("InstaPlan - Gestion des Cours");

        } catch (IOException e) {
            e.printStackTrace();
            welcomeLabel.setText("Erreur lors du chargement de la gestion des cours : " + e.getMessage());
        }
    }
    @FXML
    private void onManageCreneauxClicked() {
        try {
            // Charger la vue de gestion des créneaux
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/projets2/views/creneaux-view.fxml"));

            // Créer une nouvelle scène avec cette vue
            Scene scene = new Scene(root, 1500, 750);

            // Obtenir la fenêtre actuelle et changer sa scène
            Stage stage = (Stage) manageCreneauxButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("InstaPlan - Gestion des Créneaux");

        } catch (IOException e) {
            e.printStackTrace();
            welcomeLabel.setText("Erreur lors du chargement de la gestion des créneaux : " + e.getMessage());
        }
    }

    @FXML
    private void onNotificationsClicked() {
        try {
            // Charger la vue des notifications
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/projets2/views/notifications-view.fxml"));

            // Créer une nouvelle scène avec cette vue
            Scene scene = new Scene(root, 1500, 750);

            // Obtenir la fenêtre actuelle et changer sa scène
            Stage stage = (Stage) notificationsButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("InstaPlan - Notifications");

        } catch (IOException e) {
            e.printStackTrace();
            welcomeLabel.setText("Erreur lors du chargement des notifications : " + e.getMessage());
        }
    }
}