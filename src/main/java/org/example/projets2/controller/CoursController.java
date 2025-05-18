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
import org.example.projets2.dao.CoursDao;
import org.example.projets2.dao.JdbcCoursDao;
import org.example.projets2.dao.JdbcUtilisateurDao;
import org.example.projets2.dao.UtilisateurDao;
import org.example.projets2.model.Cours;
import org.example.projets2.model.Role;
import org.example.projets2.model.Utilisateur;
import org.example.projets2.util.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CoursController implements Initializable {

    @FXML private Button backButton;
    @FXML private TextField searchField;
    @FXML private TableView<Cours> coursTable;
    @FXML private TableColumn<Cours, Integer> idColumn;
    @FXML private TableColumn<Cours, String> nomColumn;
    @FXML private TableColumn<Cours, String> enseignantColumn;
    @FXML private TableColumn<Cours, Integer> dureeColumn;

    @FXML private Button addButton;
    @FXML private Button deleteButton;

    @FXML private TextField nomField;
    @FXML private ComboBox<Utilisateur> enseignantComboBox;
    @FXML private Spinner<Integer> dureeSpinner;
    @FXML private TextArea descriptionArea;

    @FXML private Button resetButton;
    @FXML private Button saveButton;

    private final CoursDao coursDao = new JdbcCoursDao();
    private final UtilisateurDao utilisateurDao = new JdbcUtilisateurDao();

    private ObservableList<Cours> cours;
    private FilteredList<Cours> filteredCours;
    private Cours currentCours;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Vérifier que l'utilisateur est administrateur
        Utilisateur user = Session.getCurrentUser();
        if (user == null || !user.isAdmin()) {
            showAlert("Accès refusé", "Vous devez être administrateur pour accéder à cette fonctionnalité.");
            onBackButtonClicked();
            return;
        }

        // Configurer le spinner de durée (minutes)
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(15, 240, 60, 15);
        dureeSpinner.setValueFactory(valueFactory);

        // Configurer la ComboBox d'enseignants
        configureEnseignantComboBox();

        // Configurer les colonnes du tableau
        idColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getId()).asObject());

        nomColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNom()));

        enseignantColumn.setCellValueFactory(cellData -> {
            Utilisateur enseignant = cellData.getValue().getEnseignant();
            if (enseignant == null) return new SimpleStringProperty("Non défini");
            return new SimpleStringProperty(enseignant.getFirstName() + " " + enseignant.getLastName());
        });

        dureeColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getDuree()).asObject());

        // Charger les données
        loadCours();

        // Configurer la recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredCours.setPredicate(cours -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                String nom = cours.getNom().toLowerCase();
                String enseignant = "";

                if (cours.getEnseignant() != null) {
                    enseignant = (cours.getEnseignant().getFirstName() + " " +
                            cours.getEnseignant().getLastName()).toLowerCase();
                }

                return nom.contains(lowerCaseFilter) || enseignant.contains(lowerCaseFilter);
            });
        });

        // Configurer la sélection dans le tableau
        coursTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showCoursDetails(newValue));

        // Désactiver les boutons de modification/suppression tant qu'aucun cours n'est sélectionné
        deleteButton.setDisable(true);
        coursTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> deleteButton.setDisable(newSelection == null));
    }

    /**
     * Configure la ComboBox des enseignants.
     */
    private void configureEnseignantComboBox() {
        try {
            // Récupérer tous les utilisateurs et filtrer les enseignants
            List<Utilisateur> allUsers = utilisateurDao.findAll();
            List<Utilisateur> enseignants = allUsers.stream()
                    .filter(u -> u.getRole() == Role.ENSEIGNANT || u.getRole() == Role.ADMIN)
                    .collect(Collectors.toList());

            ObservableList<Utilisateur> enseignantsList = FXCollections.observableArrayList(enseignants);
            enseignantComboBox.setItems(enseignantsList);

            // Configurer l'affichage des enseignants
            enseignantComboBox.setConverter(new StringConverter<Utilisateur>() {
                @Override
                public String toString(Utilisateur user) {
                    if (user == null) return "";
                    return user.getFirstName() + " " + user.getLastName();
                }

                @Override
                public Utilisateur fromString(String string) {
                    return null; // Pas besoin pour notre usage
                }
            });

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger la liste des enseignants: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Charge tous les cours depuis la base de données.
     */
    private void loadCours() {
        try {
            List<Cours> coursList = coursDao.findAll();
            cours = FXCollections.observableArrayList(coursList);
            filteredCours = new FilteredList<>(cours, p -> true);
            coursTable.setItems(filteredCours);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les cours: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche les détails d'un cours dans le formulaire.
     */
    private void showCoursDetails(Cours cours) {
        currentCours = cours;

        if (cours != null) {
            // Remplir les champs avec les valeurs du cours
            nomField.setText(cours.getNom());
            enseignantComboBox.setValue(cours.getEnseignant());
            dureeSpinner.getValueFactory().setValue(cours.getDuree());

            // Note: le champ description n'est pas dans le modèle actuel
            // Si on l'ajoute plus tard, on pourra afficher cours.getDescription()
            descriptionArea.setText("");
        } else {
            // Réinitialiser le formulaire
            resetForm();
        }
    }

    /**
     * Réinitialise le formulaire.
     */
    private void resetForm() {
        nomField.setText("");
        enseignantComboBox.setValue(null);
        dureeSpinner.getValueFactory().setValue(60);
        descriptionArea.setText("");
        currentCours = null;
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
        coursTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void onDeleteButtonClicked() {
        Cours selectedCours = coursTable.getSelectionModel().getSelectedItem();
        if (selectedCours == null) {
            showAlert("Erreur", "Veuillez sélectionner un cours à supprimer.");
            return;
        }

        if (showConfirmation("Confirmation", "Êtes-vous sûr de vouloir supprimer le cours " +
                selectedCours.getNom() + " ?")) {
            try {
                coursDao.delete(selectedCours.getId());
                cours.remove(selectedCours);
                resetForm();
                showAlert("Succès", "Le cours a été supprimé avec succès.");
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer le cours: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onResetButtonClicked() {
        resetForm();
        coursTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void onSaveButtonClicked() {
        // Valider les entrées
        String nom = nomField.getText().trim();
        if (nom.isEmpty()) {
            showAlert("Erreur", "Le nom du cours est obligatoire.");
            return;
        }

        Utilisateur enseignant = enseignantComboBox.getValue();
        if (enseignant == null) {
            showAlert("Erreur", "Veuillez sélectionner un enseignant.");
            return;
        }

        int duree = dureeSpinner.getValue();
        // String description = descriptionArea.getText(); // À utiliser si on ajoute le champ à la classe Cours

        try {
            if (currentCours == null) {
                // Création d'un nouveau cours
                Cours nouveauCours = new Cours(nom, enseignant, duree);
                coursDao.create(nouveauCours);
                cours.add(nouveauCours);
                showAlert("Succès", "Le cours a été créé avec succès.");
            } else {
                // Mise à jour d'un cours existant
                currentCours.setNom(nom);
                currentCours.setEnseignant(enseignant);
                currentCours.setDuree(duree);
                coursDao.update(currentCours);

                // Rafraîchir la table
                coursTable.refresh();
                showAlert("Succès", "Le cours a été mis à jour avec succès.");
            }

            // Réinitialiser le formulaire après l'opération
            resetForm();
            coursTable.getSelectionModel().clearSelection();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de sauvegarder le cours: " + e.getMessage());
            e.printStackTrace();
        }
    }
}