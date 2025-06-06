package org.example.projets2.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.example.projets2.dao.*;
import org.example.projets2.model.*;
import org.example.projets2.service.NotificationService;
import org.example.projets2.util.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CreneauxController implements Initializable {

    @FXML private Button backButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterTypeComboBox;

    @FXML private TableView<Creneau> creneauxTable;
    @FXML private TableColumn<Creneau, Integer> idColumn;
    @FXML private TableColumn<Creneau, LocalDate> dateColumn;
    @FXML private TableColumn<Creneau, LocalTime> heureDebutColumn;
    @FXML private TableColumn<Creneau, LocalTime> heureFinColumn;
    @FXML private TableColumn<Creneau, String> coursColumn;
    @FXML private TableColumn<Creneau, String> enseignantColumn;
    @FXML private TableColumn<Creneau, String> salleColumn;

    @FXML private Button addButton;
    @FXML private Button deleteButton;

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Integer> heureDebutComboBox;
    @FXML private ComboBox<Integer> minuteDebutComboBox;
    @FXML private ComboBox<Integer> heureFinComboBox;
    @FXML private ComboBox<Integer> minuteFinComboBox;
    @FXML private ComboBox<Cours> coursComboBox;
    @FXML private ComboBox<Salle> salleComboBox;
    @FXML private ChoiceBox<String> statutChoiceBox;
    @FXML private TextArea noteTextArea;

    @FXML private Label conflictLabel;
    @FXML private Button checkConflictsButton;

    @FXML private Button resetButton;
    @FXML private Button saveButton;

    private final CreneauDao creneauDao = new JdbcCreneauDao();
    private final CoursDao coursDao = new JdbcCoursDao();
    private final SalleDao salleDao = new JdbcSalleDao();

    private ObservableList<Creneau> creneaux;
    private FilteredList<Creneau> filteredCreneaux;
    private Creneau currentCreneau;

    // Formats pour l'affichage des dates et heures
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // Types de filtres disponibles
    private final ObservableList<String> filterTypes = FXCollections.observableArrayList(
            "Tous", "Aujourd'hui", "Cette semaine", "Ce mois", "Cours", "Enseignant", "Salle"
    );

    // Statuts possibles pour un créneau
    private final ObservableList<String> statuts = FXCollections.observableArrayList(
            "Planifié", "Confirmé", "Annulé", "Reporté"
    );

    private final NotificationService notificationService = new NotificationService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Vérifier que l'utilisateur est administrateur
        Utilisateur user = Session.getCurrentUser();
        if (user == null || !user.isAdmin()) {
            showAlert("Accès refusé", "Vous devez être administrateur pour accéder à cette fonctionnalité.");
            onBackButtonClicked();
            return;
        }

        // Configurer les ComboBox d'heures et minutes
        configureTimeComboBoxes();

        // Configurer les ComboBox de cours et salles
        configureCoursComboBox();
        configureSalleComboBox();

        // Configurer le filtrage
        filterTypeComboBox.setItems(filterTypes);
        filterTypeComboBox.setValue("Tous");

        // Configurer le statut
        statutChoiceBox.setItems(statuts);
        statutChoiceBox.setValue("Planifié");

        // Configurer le DatePicker avec la date du jour
        datePicker.setValue(LocalDate.now());

        // Configurer les colonnes du tableau
        configureTableColumns();

        // Charger les données
        loadCreneaux();

        // Configurer la recherche
        setupSearch();

        // Configurer les sélections dans le tableau
        creneauxTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showCreneauDetails(newValue));

        // Désactiver le bouton de suppression tant qu'aucun créneau n'est sélectionné
        deleteButton.setDisable(true);
        creneauxTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> deleteButton.setDisable(newSelection == null));
    }

    /**
     * Configure les ComboBox d'heures et minutes.
     */
    private void configureTimeComboBoxes() {
        // Heures (0-23)
        ObservableList<Integer> heures = FXCollections.observableArrayList(
                IntStream.rangeClosed(0, 23).boxed().collect(Collectors.toList())
        );
        heureDebutComboBox.setItems(heures);
        heureFinComboBox.setItems(heures);

        // Minutes (0, 15, 30, 45)
        ObservableList<Integer> minutes = FXCollections.observableArrayList(0, 15, 30, 45);
        minuteDebutComboBox.setItems(minutes);
        minuteFinComboBox.setItems(minutes);

        // Valeurs par défaut
        heureDebutComboBox.setValue(8);
        minuteDebutComboBox.setValue(0);
        heureFinComboBox.setValue(9);
        minuteFinComboBox.setValue(30);
    }

    /**
     * Configure la ComboBox des cours.
     */
    private void configureCoursComboBox() {
        try {
            List<Cours> coursList = coursDao.findAll();
            ObservableList<Cours> coursItems = FXCollections.observableArrayList(coursList);
            coursComboBox.setItems(coursItems);

            // Configurer l'affichage des cours
            coursComboBox.setConverter(new StringConverter<Cours>() {
                @Override
                public String toString(Cours cours) {
                    if (cours == null) return "";
                    return cours.getNom() + " (" + cours.getEnseignant().getFirstName() + " "
                            + cours.getEnseignant().getLastName() + ")";
                }

                @Override
                public Cours fromString(String string) {
                    return null; // Pas nécessaire pour notre usage
                }
            });

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger la liste des cours: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configure la ComboBox des salles.
     */
    private void configureSalleComboBox() {
        try {
            List<Salle> sallesList = salleDao.findAll();
            ObservableList<Salle> sallesItems = FXCollections.observableArrayList(sallesList);
            salleComboBox.setItems(sallesItems);

            // Configurer l'affichage des salles
            salleComboBox.setConverter(new StringConverter<Salle>() {
                @Override
                public String toString(Salle salle) {
                    if (salle == null) return "";
                    return salle.getNom() + " (Cap: " + salle.getCapacite() + ")";
                }

                @Override
                public Salle fromString(String string) {
                    return null; // Pas nécessaire pour notre usage
                }
            });

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger la liste des salles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configure les colonnes du tableau.
     */
    private void configureTableColumns() {
        idColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getId()).asObject());

        dateColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getDate()));
        dateColumn.setCellFactory(column -> new TableCell<Creneau, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(dateFormatter.format(date));
                }
            }
        });

        heureDebutColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getDebut()));
        heureDebutColumn.setCellFactory(column -> new TableCell<Creneau, LocalTime>() {
            @Override
            protected void updateItem(LocalTime time, boolean empty) {
                super.updateItem(time, empty);
                if (empty || time == null) {
                    setText(null);
                } else {
                    setText(timeFormatter.format(time));
                }
            }
        });

        heureFinColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getFin()));
        heureFinColumn.setCellFactory(column -> new TableCell<Creneau, LocalTime>() {
            @Override
            protected void updateItem(LocalTime time, boolean empty) {
                super.updateItem(time, empty);
                if (empty || time == null) {
                    setText(null);
                } else {
                    setText(timeFormatter.format(time));
                }
            }
        });

        coursColumn.setCellValueFactory(cellData -> {
            Cours cours = cellData.getValue().getCours();
            if (cours == null) return new SimpleStringProperty("Non défini");
            return new SimpleStringProperty(cours.getNom());
        });

        enseignantColumn.setCellValueFactory(cellData -> {
            Cours cours = cellData.getValue().getCours();
            if (cours == null || cours.getEnseignant() == null)
                return new SimpleStringProperty("Non défini");

            Utilisateur enseignant = cours.getEnseignant();
            return new SimpleStringProperty(enseignant.getFirstName() + " " + enseignant.getLastName());
        });

        salleColumn.setCellValueFactory(cellData -> {
            Salle salle = cellData.getValue().getSalle();
            if (salle == null) return new SimpleStringProperty("Non définie");
            return new SimpleStringProperty(salle.getNom());
        });
    }

    /**
     * Configure la recherche et le filtrage.
     */
    private void setupSearch() {
        // Changement du type de filtre
        filterTypeComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            applyFilters();
        });

        // Changement du texte de recherche
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            applyFilters();
        });
    }

    /**
     * Applique les filtres à la liste des créneaux.
     */
    private void applyFilters() {
        String filterType = filterTypeComboBox.getValue();
        String searchText = searchField.getText().toLowerCase();

        filteredCreneaux.setPredicate(creneau -> {
            // Si le texte de recherche est vide, on n'applique que le filtre de type
            boolean matchesSearch = true;
            if (searchText != null && !searchText.isEmpty()) {
                // Recherche par nom de cours
                String coursNom = creneau.getCours() != null ? creneau.getCours().getNom().toLowerCase() : "";

                // Recherche par nom d'enseignant
                String enseignantNom = "";
                if (creneau.getCours() != null && creneau.getCours().getEnseignant() != null) {
                    Utilisateur enseignant = creneau.getCours().getEnseignant();
                    enseignantNom = (enseignant.getFirstName() + " " + enseignant.getLastName()).toLowerCase();
                }

                // Recherche par nom de salle
                String salleNom = creneau.getSalle() != null ? creneau.getSalle().getNom().toLowerCase() : "";

                // Vérifier si un des champs correspond à la recherche
                matchesSearch = coursNom.contains(searchText) ||
                        enseignantNom.contains(searchText) ||
                        salleNom.contains(searchText);
            }

            // Appliquer le filtre par type
            boolean matchesType = true;
            LocalDate now = LocalDate.now();

            switch (filterType) {
                case "Aujourd'hui":
                    matchesType = creneau.getDate().equals(now);
                    break;
                case "Cette semaine":
                    LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
                    LocalDate endOfWeek = startOfWeek.plusDays(6);
                    matchesType = !creneau.getDate().isBefore(startOfWeek) && !creneau.getDate().isAfter(endOfWeek);
                    break;
                case "Ce mois":
                    matchesType = creneau.getDate().getMonth() == now.getMonth() &&
                            creneau.getDate().getYear() == now.getYear();
                    break;
                case "Cours":
                    // Recherche par cours (déjà gérée par searchText)
                    break;
                case "Enseignant":
                    // Recherche par enseignant (déjà gérée par searchText)
                    break;
                case "Salle":
                    // Recherche par salle (déjà gérée par searchText)
                    break;
                default: // "Tous"
                    break;
            }

            return matchesSearch && matchesType;
        });
    }

    /**
     * Charge tous les créneaux depuis la base de données.
     */
    private void loadCreneaux() {
        try {
            List<Creneau> creneauxList = creneauDao.findAll();
            creneaux = FXCollections.observableArrayList(creneauxList);
            filteredCreneaux = new FilteredList<>(creneaux, p -> true);
            creneauxTable.setItems(filteredCreneaux);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les créneaux: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche les détails d'un créneau dans le formulaire.
     */
    private void showCreneauDetails(Creneau creneau) {
        currentCreneau = creneau;

        if (creneau != null) {
            // Remplir les champs avec les valeurs du créneau
            datePicker.setValue(creneau.getDate());

            // Heures et minutes de début
            LocalTime debut = creneau.getDebut();
            heureDebutComboBox.setValue(debut.getHour());
            minuteDebutComboBox.setValue(debut.getMinute());

            // Heures et minutes de fin
            LocalTime fin = creneau.getFin();
            heureFinComboBox.setValue(fin.getHour());
            minuteFinComboBox.setValue(fin.getMinute());

            // Cours et salle
            coursComboBox.setValue(creneau.getCours());
            salleComboBox.setValue(creneau.getSalle());

            // Statut et note (à implémenter si ces champs sont ajoutés au modèle Creneau)
            statutChoiceBox.setValue("Planifié");
            noteTextArea.setText("");

            // Mise à jour du label de conflit (pour l'instant, pas de vérification)
            conflictLabel.setText("Vérifiez les conflits en cliquant sur le bouton ci-dessous");
            conflictLabel.setStyle("-fx-text-fill: #888888;");
        } else {
            // Réinitialiser le formulaire
            resetForm();
        }
    }

    /**
     * Réinitialise le formulaire.
     */
    private void resetForm() {
        datePicker.setValue(LocalDate.now());
        heureDebutComboBox.setValue(8);
        minuteDebutComboBox.setValue(0);
        heureFinComboBox.setValue(9);
        minuteFinComboBox.setValue(30);
        coursComboBox.setValue(null);
        salleComboBox.setValue(null);
        statutChoiceBox.setValue("Planifié");
        noteTextArea.setText("");

        // Réinitialiser le label de conflit
        conflictLabel.setText("Aucune vérification effectuée");
        conflictLabel.setStyle("-fx-text-fill: #888888;");

        currentCreneau = null;
    }

    /**
     * Vérifie s'il y a des conflits avec le créneau en cours d'édition.
     * Un conflit existe si :
     * - La même salle est utilisée au même moment
     * - Le même enseignant a deux cours au même moment
     */
    @FXML
    private void onCheckConflictsButtonClicked() {
        // Récupérer les valeurs du formulaire
        LocalDate date = datePicker.getValue();
        if (date == null) {
            showAlert("Erreur", "Veuillez sélectionner une date.");
            return;
        }

        LocalTime debut = LocalTime.of(
                heureDebutComboBox.getValue(),
                minuteDebutComboBox.getValue()
        );

        LocalTime fin = LocalTime.of(
                heureFinComboBox.getValue(),
                minuteFinComboBox.getValue()
        );

        if (fin.isBefore(debut) || fin.equals(debut)) {
            conflictLabel.setText("L'heure de fin doit être après l'heure de début !");
            conflictLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        Cours cours = coursComboBox.getValue();
        if (cours == null) {
            showAlert("Erreur", "Veuillez sélectionner un cours.");
            return;
        }

        Salle salle = salleComboBox.getValue();
        if (salle == null) {
            showAlert("Erreur", "Veuillez sélectionner une salle.");
            return;
        }

        try {
            // Rechercher les conflits de salle
            List<Creneau> creneauxMemeSalle = creneauDao.findBySalle(salle.getId());

            // Trouver le créneau en conflit (pour notification)
            Creneau creneauConflitSalle = null;

            boolean conflitSalle = false;
            for (Creneau c : creneauxMemeSalle) {
                if (c.getDate().equals(date) && // Même jour
                        (currentCreneau == null || c.getId() != currentCreneau.getId()) && // Pas le créneau courant
                        (debut.isBefore(c.getFin()) && fin.isAfter(c.getDebut()))) { // Horaires se chevauchent
                    conflitSalle = true;
                    creneauConflitSalle = c;
                    break;
                }
            }

            // Rechercher les conflits d'enseignant
            int enseignantId = cours.getEnseignant().getId();
            List<Creneau> creneauxMemeEnseignant = creneauDao.findAll().stream()
                    .filter(c -> c.getCours() != null && c.getCours().getEnseignant() != null)
                    .filter(c -> c.getCours().getEnseignant().getId() == enseignantId)
                    .filter(c -> c.getDate().equals(date)) // Même jour
                    .filter(c -> currentCreneau == null || c.getId() != currentCreneau.getId()) // Pas le créneau courant
                    .collect(Collectors.toList());

            // Trouver le créneau en conflit (pour notification)
            Creneau creneauConflitEnseignant = null;

            boolean conflitEnseignant = false;
            for (Creneau c : creneauxMemeEnseignant) {
                if (debut.isBefore(c.getFin()) && fin.isAfter(c.getDebut())) { // Horaires se chevauchent
                    conflitEnseignant = true;
                    creneauConflitEnseignant = c;
                    break;
                }
            }

            // Afficher le résultat des vérifications
            if (conflitSalle || conflitEnseignant) {
                StringBuilder message = new StringBuilder("⚠️ Conflits détectés : ");
                if (conflitSalle) message.append("La salle est déjà occupée. ");
                if (conflitEnseignant) message.append("L'enseignant a déjà un cours. ");

                conflictLabel.setText(message.toString());
                conflictLabel.setStyle("-fx-text-fill: red;");

                // Notifier les administrateurs des conflits
                Utilisateur admin = Session.getCurrentUser(); // L'admin actuel

                if (conflitSalle && creneauConflitSalle != null) {
                    // Créer une notification de conflit de salle
                    notificationService.notifierConflitCreneau(
                            creneauConflitSalle,
                            currentCreneau != null ? currentCreneau :
                                    new Creneau(date, debut, fin, cours, salle),
                            admin,
                            "Conflit de salle"
                    );
                }

                if (conflitEnseignant && creneauConflitEnseignant != null) {
                    // Créer une notification de conflit d'enseignant
                    notificationService.notifierConflitCreneau(
                            creneauConflitEnseignant,
                            currentCreneau != null ? currentCreneau :
                                    new Creneau(date, debut, fin, cours, salle),
                            admin,
                            "Conflit d'enseignant"
                    );
                }

                // Notifier également tous les administrateurs
                if (conflitSalle && creneauConflitSalle != null) {
                    notificationService.notifierTousAdmins(
                            creneauConflitSalle,
                            currentCreneau != null ? currentCreneau :
                                    new Creneau(date, debut, fin, cours, salle),
                            "Conflit de salle"
                    );
                } else if (conflitEnseignant && creneauConflitEnseignant != null) {
                    notificationService.notifierTousAdmins(
                            creneauConflitEnseignant,
                            currentCreneau != null ? currentCreneau :
                                    new Creneau(date, debut, fin, cours, salle),
                            "Conflit d'enseignant"
                    );
                }

            } else {
                conflictLabel.setText("✅ Aucun conflit détecté !");
                conflictLabel.setStyle("-fx-text-fill: green;");
            }

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de vérifier les conflits: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche une boîte de dialogue d'alerte.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une boîte de dialogue de confirmation.
     */
    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    @FXML
    private void onBackButtonClicked() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/projets2/views/planning-view.fxml"));
            Scene scene = new Scene(root, 1500, 750);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("InstaPlan - Planning");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la vue Planning: " + e.getMessage());
        }
    }

    @FXML
    private void onAddButtonClicked() {
        resetForm();
        creneauxTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void onDeleteButtonClicked() {
        Creneau selectedCreneau = creneauxTable.getSelectionModel().getSelectedItem();
        if (selectedCreneau == null) {
            showAlert("Erreur", "Veuillez sélectionner un créneau à supprimer.");
            return;
        }

        if (showConfirmation("Confirmation", "Êtes-vous sûr de vouloir supprimer ce créneau ?")) {
            try {
                // Garder une référence au créneau et à l'enseignant avant suppression
                Creneau creneauToDelete = selectedCreneau;
                Utilisateur enseignant = creneauToDelete.getCours().getEnseignant();

                creneauDao.delete(selectedCreneau.getId());
                creneaux.remove(selectedCreneau);

                // Notifier l'enseignant de l'annulation
                notificationService.notifierAnnulationCreneau(creneauToDelete, enseignant);

                // Notifier tous les étudiants
                try {
                    notificationService.notifierTousEtudiants(creneauToDelete);
                } catch (SQLException e) {
                    // Log l'erreur mais continuer quand même
                    System.err.println("Erreur lors de la notification des étudiants: " + e.getMessage());
                }

                resetForm();
                showAlert("Succès", "Le créneau a été supprimé avec succès et les utilisateurs ont été notifiés.");
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer le créneau: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onResetButtonClicked() {
        resetForm();
        creneauxTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void onSaveButtonClicked() {
        // Récupérer les valeurs du formulaire
        LocalDate date = datePicker.getValue();
        if (date == null) {
            showAlert("Erreur", "Veuillez sélectionner une date.");
            return;
        }

        LocalTime debut = LocalTime.of(
                heureDebutComboBox.getValue(),
                minuteDebutComboBox.getValue()
        );

        LocalTime fin = LocalTime.of(
                heureFinComboBox.getValue(),
                minuteFinComboBox.getValue()
        );

        if (fin.isBefore(debut) || fin.equals(debut)) {
            showAlert("Erreur", "L'heure de fin doit être après l'heure de début.");
            return;
        }

        Cours cours = coursComboBox.getValue();
        if (cours == null) {
            showAlert("Erreur", "Veuillez sélectionner un cours.");
            return;
        }

        Salle salle = salleComboBox.getValue();
        if (salle == null) {
            showAlert("Erreur", "Veuillez sélectionner une salle.");
            return;
        }

        try {
            if (currentCreneau == null) {
                // Création d'un nouveau créneau
                Creneau nouveauCreneau = new Creneau(date, debut, fin, cours, salle);
                creneauDao.create(nouveauCreneau);
                creneaux.add(nouveauCreneau);

                // Notifier l'enseignant du cours
                notificationService.notifierCreationCreneau(nouveauCreneau, cours.getEnseignant());

                // Notifier tous les étudiants
                notificationService.notifierTousEtudiants(nouveauCreneau);

                showAlert("Succès", "Le créneau a été créé avec succès et les notifications ont été envoyées.");
            } else {
                // Sauvegarder l'état avant modification pour détecter les changements
                LocalDate oldDate = currentCreneau.getDate();
                LocalTime oldDebut = currentCreneau.getDebut();
                LocalTime oldFin = currentCreneau.getFin();
                Salle oldSalle = currentCreneau.getSalle();
                Cours oldCours = currentCreneau.getCours();

                // Mise à jour d'un créneau existant
                currentCreneau.setDate(date);
                currentCreneau.setDebut(debut);
                currentCreneau.setFin(fin);
                currentCreneau.setCours(cours);
                currentCreneau.setSalle(salle);
                creneauDao.update(currentCreneau);

                // Détecter les changements
                StringBuilder detailsModification = new StringBuilder();
                if (!oldDate.equals(date)) {
                    detailsModification.append("Date modifiée. ");
                }
                if (!oldDebut.equals(debut) || !oldFin.equals(oldFin)) {
                    detailsModification.append("Horaire modifié. ");
                }
                if (!oldSalle.equals(salle)) {
                    detailsModification.append("Salle modifiée. ");
                }
                if (!oldCours.equals(cours)) {
                    detailsModification.append("Cours modifié. ");

                    // Si l'enseignant a changé, notifier l'ancien et le nouveau
                    if (!oldCours.getEnseignant().equals(cours.getEnseignant())) {
                        // Notifier l'ancien enseignant
                        notificationService.notifierAnnulationCreneau(currentCreneau, oldCours.getEnseignant());

                        // Notifier le nouvel enseignant
                        notificationService.notifierCreationCreneau(currentCreneau, cours.getEnseignant());
                    }
                }

                // Notifier l'enseignant du cours des modifications
                notificationService.notifierModificationCreneau(
                        currentCreneau,
                        cours.getEnseignant(),
                        detailsModification.toString()
                );

                // Notifier tous les étudiants
                notificationService.notifierTousEtudiants(currentCreneau);

                // Rafraîchir la table
                creneauxTable.refresh();
                showAlert("Succès", "Le créneau a été mis à jour avec succès et les notifications ont été envoyées.");
            }

            // Réinitialiser le formulaire après l'opération
            resetForm();
            creneauxTable.getSelectionModel().clearSelection();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de sauvegarder le créneau: " + e.getMessage());
            e.printStackTrace();
        }
    }
}