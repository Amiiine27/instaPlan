package org.example.projets2.controller;

import javafx.beans.property.SimpleIntegerProperty;
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
import org.example.projets2.dao.JdbcSalleDao;
import org.example.projets2.dao.SalleDao;
import org.example.projets2.model.Role;
import org.example.projets2.model.Salle;
import org.example.projets2.model.Utilisateur;
import org.example.projets2.util.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class SallesController implements Initializable {

    @FXML private Button backButton;
    @FXML private TextField searchField;
    @FXML private TableView<Salle> sallesTable;
    @FXML private TableColumn<Salle, Integer> idColumn;
    @FXML private TableColumn<Salle, String> nomColumn;
    @FXML private TableColumn<Salle, Integer> capaciteColumn;
    @FXML private TableColumn<Salle, String> equipementsColumn;

    @FXML private Button addButton;
    @FXML private Button deleteButton;

    @FXML private TextField nomField;
    @FXML private Spinner<Integer> capaciteSpinner;
    @FXML private CheckBox projecteurCheck;
    @FXML private CheckBox ordinateursCheck;
    @FXML private CheckBox tableauxCheck;
    @FXML private CheckBox climatisationCheck;

    @FXML private Button resetButton;
    @FXML private Button saveButton;

    private final SalleDao salleDao = new JdbcSalleDao();
    private ObservableList<Salle> salles;
    private FilteredList<Salle> filteredSalles;
    private Salle currentSalle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Vérifier que l'utilisateur est administrateur
        Utilisateur user = Session.getCurrentUser();
        if (user == null || !user.isAdmin()) {
            showAlert("Accès refusé", "Vous devez être administrateur pour accéder à cette fonctionnalité.");
            onBackButtonClicked();
            return;
        }

        // Configurer le spinner de capacité
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 30);
        capaciteSpinner.setValueFactory(valueFactory);

        // Configurer les colonnes du tableau
        idColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        nomColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNom()));
        capaciteColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getCapacite()).asObject());
        equipementsColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEquipements()));

        // Charger les données
        loadSalles();

        // Configurer la recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredSalles.setPredicate(salle -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                if (salle.getNom().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else return salle.getEquipements().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // Configurer la sélection dans le tableau
        sallesTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showSalleDetails(newValue));

        // Désactiver les boutons de modification/suppression tant qu'aucune salle n'est sélectionnée
        deleteButton.setDisable(true);
        sallesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> deleteButton.setDisable(newSelection == null));
    }

    /**
     * Charge toutes les salles depuis la base de données.
     */
    private void loadSalles() {
        try {
            List<Salle> sallesList = salleDao.findAll();
            salles = FXCollections.observableArrayList(sallesList);
            filteredSalles = new FilteredList<>(salles, p -> true);
            sallesTable.setItems(filteredSalles);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les salles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche les détails d'une salle dans le formulaire.
     */
    private void showSalleDetails(Salle salle) {
        currentSalle = salle;

        if (salle != null) {
            // Remplir les champs avec les valeurs de la salle
            nomField.setText(salle.getNom());
            capaciteSpinner.getValueFactory().setValue(salle.getCapacite());

            // Analyser les équipements (séparés par des virgules)
            String equipements = salle.getEquipements();
            projecteurCheck.setSelected(equipements.contains("Projecteur"));
            ordinateursCheck.setSelected(equipements.contains("Ordinateurs"));
            tableauxCheck.setSelected(equipements.contains("Tableaux"));
            climatisationCheck.setSelected(equipements.contains("Climatisation"));
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
        capaciteSpinner.getValueFactory().setValue(30);
        projecteurCheck.setSelected(false);
        ordinateursCheck.setSelected(false);
        tableauxCheck.setSelected(false);
        climatisationCheck.setSelected(false);
        currentSalle = null;
    }

    /**
     * Collecte les équipements sélectionnés dans les checkboxes.
     */
    private String collectEquipements() {
        List<String> equipList = new ArrayList<>();

        if (projecteurCheck.isSelected()) equipList.add("Projecteur");
        if (ordinateursCheck.isSelected()) equipList.add("Ordinateurs");
        if (tableauxCheck.isSelected()) equipList.add("Tableaux");
        if (climatisationCheck.isSelected()) equipList.add("Climatisation");

        return String.join(", ", equipList);
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
        sallesTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void onDeleteButtonClicked() {
        Salle selectedSalle = sallesTable.getSelectionModel().getSelectedItem();
        if (selectedSalle == null) {
            showAlert("Erreur", "Veuillez sélectionner une salle à supprimer.");
            return;
        }

        if (showConfirmation("Confirmation", "Êtes-vous sûr de vouloir supprimer la salle " +
                selectedSalle.getNom() + " ?")) {
            try {
                salleDao.delete(selectedSalle.getId());
                salles.remove(selectedSalle);
                resetForm();
                showAlert("Succès", "La salle a été supprimée avec succès.");
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer la salle: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onResetButtonClicked() {
        resetForm();
        sallesTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void onSaveButtonClicked() {
        // Valider les entrées
        String nom = nomField.getText().trim();
        if (nom.isEmpty()) {
            showAlert("Erreur", "Le nom de la salle est obligatoire.");
            return;
        }

        int capacite = capaciteSpinner.getValue();
        String equipements = collectEquipements();

        try {
            if (currentSalle == null) {
                // Création d'une nouvelle salle
                Salle nouvelleSalle = new Salle(nom, capacite, equipements);
                salleDao.create(nouvelleSalle);
                salles.add(nouvelleSalle);
                showAlert("Succès", "La salle a été créée avec succès.");
            } else {
                // Mise à jour d'une salle existante
                currentSalle.setNom(nom);
                currentSalle.setCapacite(capacite);
                currentSalle.setEquipements(equipements);
                salleDao.update(currentSalle);

                // Rafraîchir la table
                sallesTable.refresh();
                showAlert("Succès", "La salle a été mise à jour avec succès.");
            }

            // Réinitialiser le formulaire après l'opération
            resetForm();
            sallesTable.getSelectionModel().clearSelection();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de sauvegarder la salle: " + e.getMessage());
            e.printStackTrace();
        }
    }
}