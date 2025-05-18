package org.example.projets2.dao;

import org.example.projets2.model.Salle;

import java.sql.SQLException;
import java.util.List;

/**
 * API de persistance pour les objets Salle.
 */
public interface SalleDao {

    /**
     * Insère une nouvelle salle et met à jour son ID auto-généré.
     */
    void create(Salle s) throws SQLException;

    /**
     * Retourne la salle d’après son identifiant, ou null si absente.
     */
    Salle findById(int id) throws SQLException;

    /**
     * Retourne la liste de toutes les salles en base.
     */
    List<Salle> findAll() throws SQLException;

    /**
     * Met à jour les champs (nom, capacité, équipements) d’une salle existante.
     */
    void update(Salle s) throws SQLException;

    /**
     * Supprime une salle d’après son identifiant.
     */
    void delete(int id) throws SQLException;
}