package org.example.projets2.service;

import org.example.projets2.dao.JdbcNotificationDao;
import org.example.projets2.dao.NotificationDao;
import org.example.projets2.dao.UtilisateurDao;
import org.example.projets2.dao.JdbcUtilisateurDao;
import org.example.projets2.model.Creneau;
import org.example.projets2.model.Notification;
import org.example.projets2.model.Role;
import org.example.projets2.model.Utilisateur;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des notifications.
 */
public class NotificationService {

    private final NotificationDao notificationDao;
    private final UtilisateurDao utilisateurDao;

    /**
     * Constructeur par défaut.
     */
    public NotificationService() {
        this.notificationDao = new JdbcNotificationDao();
        this.utilisateurDao = new JdbcUtilisateurDao();
    }

    /**
     * Crée une notification pour un utilisateur concernant la création d'un créneau.
     */
    public void notifierCreationCreneau(Creneau creneau, Utilisateur destinataire) throws SQLException {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String titre = "Nouveau cours planifié";
        String message = "Un nouveau cours \"" + creneau.getCours().getNom() + "\" a été planifié le " +
                dateFormatter.format(creneau.getDate()) + " de " +
                timeFormatter.format(creneau.getDebut()) + " à " +
                timeFormatter.format(creneau.getFin()) + " en salle " +
                creneau.getSalle().getNom() + ".";

        Notification notification = new Notification(
                titre, message, Notification.Type.CREATION, destinataire, creneau
        );

        notificationDao.create(notification);
    }

    /**
     * Crée une notification pour un utilisateur concernant la modification d'un créneau.
     */
    public void notifierModificationCreneau(Creneau creneau, Utilisateur destinataire, String detailsModification) throws SQLException {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String titre = "Modification de cours";
        String message = "Le cours \"" + creneau.getCours().getNom() + "\" du " +
                dateFormatter.format(creneau.getDate()) + " a été modifié. " +
                detailsModification;

        Notification notification = new Notification(
                titre, message, Notification.Type.MODIFICATION, destinataire, creneau
        );

        notificationDao.create(notification);
    }

    /**
     * Crée une notification pour un utilisateur concernant l'annulation d'un créneau.
     */
    public void notifierAnnulationCreneau(Creneau creneau, Utilisateur destinataire) throws SQLException {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String titre = "Annulation de cours";
        String message = "Le cours \"" + creneau.getCours().getNom() + "\" du " +
                dateFormatter.format(creneau.getDate()) + " de " +
                timeFormatter.format(creneau.getDebut()) + " à " +
                timeFormatter.format(creneau.getFin()) + " a été annulé.";

        Notification notification = new Notification(
                titre, message, Notification.Type.ANNULATION, destinataire, creneau
        );

        notificationDao.create(notification);
    }

    /**
     * Crée une notification pour un administrateur concernant un conflit détecté.
     */
    public void notifierConflitCreneau(Creneau creneau1, Creneau creneau2, Utilisateur destinataire, String typeConflit) throws SQLException {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String titre = "Conflit détecté";
        String message = "Un conflit a été détecté : " + typeConflit + " le " +
                dateFormatter.format(creneau1.getDate()) + " entre les cours \"" +
                creneau1.getCours().getNom() + "\" et \"" + creneau2.getCours().getNom() + "\".";

        Notification notification = new Notification(
                titre, message, Notification.Type.CONFLIT, destinataire
        );

        notificationDao.create(notification);
    }

    /**
     * Notifie tous les étudiants de la création d'un nouveau créneau.
     */
    public void notifierTousEtudiants(Creneau creneau) throws SQLException {
        List<Utilisateur> etudiants = utilisateurDao.findAll().stream()
                .filter(u -> u.getRole() == Role.ETUDIANT)
                .collect(Collectors.toList());

        for (Utilisateur etudiant : etudiants) {
            notifierCreationCreneau(creneau, etudiant);
        }
    }

    /**
     * Notifie tous les administrateurs d'un conflit détecté.
     */
    public void notifierTousAdmins(Creneau creneau1, Creneau creneau2, String typeConflit) throws SQLException {
        List<Utilisateur> admins = utilisateurDao.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .collect(Collectors.toList());

        for (Utilisateur admin : admins) {
            notifierConflitCreneau(creneau1, creneau2, admin, typeConflit);
        }
    }

    /**
     * Récupère toutes les notifications d'un utilisateur.
     */
    public List<Notification> getNotificationsUtilisateur(int utilisateurId) throws SQLException {
        return notificationDao.findByDestinataire(utilisateurId);
    }

    /**
     * Récupère les notifications non lues d'un utilisateur.
     */
    public List<Notification> getNotificationsNonLues(int utilisateurId) throws SQLException {
        return notificationDao.findNonLuesByDestinataire(utilisateurId);
    }

    /**
     * Marque une notification comme lue.
     */
    public void marquerCommeLue(int notificationId) throws SQLException {
        notificationDao.markAsRead(notificationId);
    }

    /**
     * Supprime une notification.
     */
    public void supprimerNotification(int notificationId) throws SQLException {
        notificationDao.delete(notificationId);
    }
}