package org.example.projets2.dao;

import org.example.projets2.model.Cours;

import java.sql.SQLException;
import java.util.List;

/**
 * API de persistance pour les objets Cours.
 */
public interface CoursDao {

    /**
     * Insère un nouveau cours et met à jour son ID auto-généré.
     */
    void create(Cours c) throws SQLException;

    /**
     * Retourne le cours d'après son identifiant, ou null si absent.
     */
    Cours findById(int id) throws SQLException;

    /**
     * Retourne la liste de tous les cours en base.
     */
    List<Cours> findAll() throws SQLException;

    /**
     * Met à jour les champs (nom, enseignant, durée) d’un cours existant.
     */
    void update(Cours c) throws SQLException;

    /**
     * Supprime un cours d’après son identifiant.
     */
    void delete(int id) throws SQLException;
}