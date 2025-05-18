package org.example.projets2.dao;

import org.example.projets2.model.Creneau;

import java.sql.SQLException;
import java.util.List;

/**
 * API de persistance pour les objets Creneau.
 */
public interface CreneauDao {

    /**
     * Insère un nouveau créneau et met à jour son ID auto-généré.
     */
    void create(Creneau c) throws SQLException;

    /**
     * Retourne le créneau d’après son identifiant, ou null si absent.
     */
    Creneau findById(int id) throws SQLException;

    /**
     * Retourne la liste de tous les créneaux en base.
     */
    List<Creneau> findAll() throws SQLException;

    /**
     * Retourne tous les créneaux liés à un cours donné.
     */
    List<Creneau> findByCours(int coursId) throws SQLException;

    /**
     * Retourne tous les créneaux pour une salle donnée.
     */
    List<Creneau> findBySalle(int salleId) throws SQLException;

    /**
     * Met à jour un créneau existant (date, horaires, cours, salle).
     */
    void update(Creneau c) throws SQLException;

    /**
     * Supprime un créneau d’après son identifiant.
     */
    void delete(int id) throws SQLException;
}