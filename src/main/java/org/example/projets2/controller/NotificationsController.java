package org.example.projets2.controller;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.projets2.model.Creneau;
import org.example.projets2.model.Notification;
import org.example.projets2.service.NotificationService;
import org.example.projets2.util.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class NotificationsController implements Initializable {

    @FXML private Button backButton;
    @FXML private ComboBox<String> filterTypeComboBox;
    @FXML private ToggleButton showOnlyUnreadToggle;

    @FXML private ListView<Notification> notificationsListView;

    @FXML private Button markAllAsReadButton;
    @FXML private Button deleteSelectedButton;

    @FXML private Label typeLabel;
    @FXML private Label dateLabel;
    @FXML private Label titreLabel;
    @FXML private TextArea messageTextArea;

    @FXML private Button markAsReadButton;
    @FXML private Button deleteButton;
    @FXML private Button viewCreneauButton;

    private final NotificationService notificationService = new NotificationService();

    private ObservableList<Notification> notifications;
    private FilteredList<Notification> filteredNotifications;

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuration des filtres
        filterTypeComboBox.setItems(FXCollections.observableArrayList(
                "Tous", "Info", "Création", "Modification", "Annulation", "Conflit"
        ));
        filterTypeComboBox.setValue("Tous");

        // Configurer la sélection dans la liste
        notificationsListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showNotificationDetails(newValue));

        // Configuration de l'affichage des notifications dans la ListView
        notificationsListView.setCellFactory(createNotificationCellFactory());

        // Chargement des notifications
        loadNotifications();

        // Configuration des filtres
        setupFilters();

        // Désactiver les boutons tant qu'aucune notification n'est sélectionnée
        markAsReadButton.setDisable(true);
        deleteButton.setDisable(true);
        viewCreneauButton.setVisible(false);

        notificationsListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    boolean hasSelection = newVal != null;
                    markAsReadButton.setDisable(!hasSelection);
                    deleteButton.setDisable(!hasSelection);

                    // Afficher le bouton "Voir le créneau" seulement si la notification a un créneau associé
                    if (hasSelection && newVal.getCreneau() != null) {
                        viewCreneauButton.setVisible(true);
                    } else {
                        viewCreneauButton.setVisible(false);
                    }
                });
    }

    /**
     * Crée une fabrique de cellules personnalisée pour l'affichage des notifications.
     */
    private Callback<ListView<Notification>, ListCell<Notification>> createNotificationCellFactory() {
        return listView -> new ListCell<Notification>() {
            @Override
            protected void updateItem(Notification notification, boolean empty) {
                super.updateItem(notification, empty);

                if (empty || notification == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("notification-info", "notification-creation",
                            "notification-modification", "notification-annulation",
                            "notification-conflit", "notification-unread");
                } else {
                    // Créer la cellule personnalisée
                    VBox container = new VBox(5);

                    Label titleLabel = new Label(notification.getTitre());
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                    Label dateLabel = new Label(notification.getDateCreation().format(dateTimeFormatter));
                    dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #777777;");

                    Label messagePreview = new Label(notification.getMessage().length() > 50 ?
                            notification.getMessage().substring(0, 50) + "..." : notification.getMessage());
                    messagePreview.setStyle("-fx-font-size: 13px;");

                    container.getChildren().addAll(titleLabel, dateLabel, messagePreview);

                    setGraphic(container);

                    // Ajouter des classes CSS selon le type et l'état de la notification
                    getStyleClass().removeAll("notification-info", "notification-creation",
                            "notification-modification", "notification-annulation",
                            "notification-conflit", "notification-unread");

                    // Ajouter la classe pour le type
                    switch (notification.getType()) {
                        case INFO:
                            getStyleClass().add("notification-info");
                            break;
                        case CREATION:
                            getStyleClass().add("notification-creation");
                            break;
                        case MODIFICATION:
                            getStyleClass().add("notification-modification");
                            break;
                        case ANNULATION:
                            getStyleClass().add("notification-annulation");
                            break;
                        case CONFLIT:
                            getStyleClass().add("notification-conflit");
                            break;
                    }

                    // Ajouter la classe pour les notifications non lues
                    if (!notification.isLue()) {
                        getStyleClass().add("notification-unread");
                    }
                }
            }
        };
    }

    /**
     * Charge les notifications de l'utilisateur courant.
     */
    private void loadNotifications() {
        try {
            int userId = Session.getCurrentUser().getId();
            List<Notification> notifList = notificationService.getNotificationsUtilisateur(userId);

            notifications = FXCollections.observableArrayList(notifList);
            filteredNotifications = new FilteredList<>(notifications, p -> true);
            notificationsListView.setItems(filteredNotifications);

            // Mettre à jour le texte du bouton de filtrage
            updateUnreadFilterButton();

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configure les filtres sur les notifications.
     */
    private void setupFilters() {
        // Filtre par type
        filterTypeComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            applyFilters();
        });

        // Filtre par statut de lecture
        showOnlyUnreadToggle.selectedProperty().addListener((obs, oldValue, newValue) -> {
            applyFilters();
        });
    }

    /**
     * Applique les filtres sélectionnés.
     */
    private void applyFilters() {
        String selectedType = filterTypeComboBox.getValue();
        boolean showOnlyUnread = showOnlyUnreadToggle.isSelected();

        filteredNotifications.setPredicate(notification -> {
            // Filtre par type
            boolean matchesType = true;
            if (!"Tous".equals(selectedType)) {
                switch (selectedType) {
                    case "Info":
                        matchesType = notification.getType() == Notification.Type.INFO;
                        break;
                    case "Création":
                        matchesType = notification.getType() == Notification.Type.CREATION;
                        break;
                    case "Modification":
                        matchesType = notification.getType() == Notification.Type.MODIFICATION;
                        break;
                    case "Annulation":
                        matchesType = notification.getType() == Notification.Type.ANNULATION;
                        break;
                    case "Conflit":
                        matchesType = notification.getType() == Notification.Type.CONFLIT;
                        break;
                }
            }

            // Filtre par statut de lecture
            boolean matchesReadStatus = !showOnlyUnread || !notification.isLue();

            return matchesType && matchesReadStatus;
        });
    }

    /**
     * Met à jour le texte du bouton de filtrage des notifications non lues.
     */
    private void updateUnreadFilterButton() {
        try {
            int userId = Session.getCurrentUser().getId();
            List<Notification> unreadList = notificationService.getNotificationsNonLues(userId);
            int unreadCount = unreadList.size();

            showOnlyUnreadToggle.setText("Non lues uniquement (" + unreadCount + ")");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Affiche les détails d'une notification.
     */
    private void showNotificationDetails(Notification notification) {
        if (notification != null) {
            // Afficher les détails
            typeLabel.setText(notification.getType().name());
            dateLabel.setText(notification.getDateCreation().format(dateTimeFormatter));
            titreLabel.setText(notification.getTitre());
            messageTextArea.setText(notification.getMessage());

            // Activer/désactiver le bouton de marquage comme lu
            markAsReadButton.setDisable(notification.isLue());

            // Afficher le bouton "Voir le créneau" si applicable
            viewCreneauButton.setVisible(notification.getCreneau() != null);

        } else {
            // Réinitialiser l'affichage
            typeLabel.setText("");
            dateLabel.setText("");
            titreLabel.setText("");
            messageTextArea.setText("");
            markAsReadButton.setDisable(true);
            viewCreneauButton.setVisible(false);
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
    private void onMarkAsReadClicked() {
        Notification selectedNotification = notificationsListView.getSelectionModel().getSelectedItem();
        if (selectedNotification == null) {
            return;
        }

        try {
            notificationService.marquerCommeLue(selectedNotification.getId());

            // Mettre à jour l'objet dans la liste
            selectedNotification.setLue(true);

            // Rafraîchir la vue
            notificationsListView.refresh();
            updateUnreadFilterButton();

            // Désactiver le bouton
            markAsReadButton.setDisable(true);

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de marquer la notification comme lue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onDeleteClicked() {
        Notification selectedNotification = notificationsListView.getSelectionModel().getSelectedItem();
        if (selectedNotification == null) {
            return;
        }

        if (showConfirmation("Confirmation", "Êtes-vous sûr de vouloir supprimer cette notification ?")) {
            try {
                notificationService.supprimerNotification(selectedNotification.getId());

                // Supprimer de la liste
                notifications.remove(selectedNotification);

                // Rafraîchir la vue
                updateUnreadFilterButton();

            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer la notification: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onMarkAllAsReadClicked() {
        if (showConfirmation("Confirmation", "Êtes-vous sûr de vouloir marquer toutes les notifications comme lues ?")) {
            try {
                // Récupérer toutes les notifications non lues
                List<Notification> nonLues = notifications.stream()
                        .filter(n -> !n.isLue())
                        .collect(Collectors.toList());

                // Marquer chacune comme lue
                for (Notification n : nonLues) {
                    notificationService.marquerCommeLue(n.getId());
                    n.setLue(true);
                }

                // Rafraîchir la vue
                notificationsListView.refresh();
                updateUnreadFilterButton();

                // Désactiver le bouton si la notification sélectionnée est maintenant lue
                Notification selectedNotification = notificationsListView.getSelectionModel().getSelectedItem();
                if (selectedNotification != null) {
                    markAsReadButton.setDisable(true);
                }

            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de marquer les notifications comme lues: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onDeleteSelectedClicked() {
        List<Notification> selectedNotifications = notificationsListView.getSelectionModel().getSelectedItems();
        if (selectedNotifications.isEmpty()) {
            showAlert("Information", "Veuillez sélectionner au moins une notification.");
            return;
        }

        if (showConfirmation("Confirmation", "Êtes-vous sûr de vouloir supprimer les notifications sélectionnées ?")) {
            try {
                // Supprimer chaque notification sélectionnée
                for (Notification n : selectedNotifications) {
                    notificationService.supprimerNotification(n.getId());
                }

                // Supprimer de la liste
                notifications.removeAll(selectedNotifications);

                // Rafraîchir la vue
                updateUnreadFilterButton();

            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer les notifications: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onViewCreneauClicked() {
        Notification selectedNotification = notificationsListView.getSelectionModel().getSelectedItem();
        if (selectedNotification == null || selectedNotification.getCreneau() == null) {
            return;
        }

        // Ici, nous pourrions ouvrir une vue détaillée du créneau
        // Pour l'instant, affichons juste une alerte avec les infos
        Creneau creneau = selectedNotification.getCreneau();

        String message = "Créneau #" + creneau.getId() + "\n" +
                "Date: " + creneau.getDate() + "\n" +
                "Horaire: " + creneau.getDebut() + " - " + creneau.getFin() + "\n" +
                "Cours: " + creneau.getCours().getNom() + "\n" +
                "Enseignant: " + creneau.getCours().getEnseignant().getFirstName() + " " +
                creneau.getCours().getEnseignant().getLastName() + "\n" +
                "Salle: " + creneau.getSalle().getNom();

        showAlert("Détails du créneau", message);
    }
}