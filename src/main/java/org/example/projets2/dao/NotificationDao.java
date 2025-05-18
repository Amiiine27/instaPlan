package org.example.projets2.dao;

import org.example.projets2.model.Notification;
import org.example.projets2.model.Utilisateur;

import java.sql.SQLException;
import java.util.List;

/**
 * API de persistance pour les objets Notification.
 */
public interface NotificationDao {

    /**
     * Insère une nouvelle notification et met à jour son ID auto-généré.
     */
    void create(Notification notification) throws SQLException;

    /**
     * Retourne la notification d'après son identifiant, ou null si absente.
     */
    Notification findById(int id) throws SQLException;

    /**
     * Retourne la liste de toutes les notifications en base.
     */
    List<Notification> findAll() throws SQLException;

    /**
     * Retourne les notifications pour un utilisateur donné.
     */
    List<Notification> findByDestinataire(int destinataireId) throws SQLException;

    /**
     * Retourne les notifications non lues pour un utilisateur donné.
     */
    List<Notification> findNonLuesByDestinataire(int destinataireId) throws SQLException;

    /**
     * Met à jour une notification existante.
     */
    void update(Notification notification) throws SQLException;

    /**
     * Marque comme lue une notification.
     */
    void markAsRead(int id) throws SQLException;

    /**
     * Supprime une notification d'après son identifiant.
     */
    void delete(int id) throws SQLException;
}