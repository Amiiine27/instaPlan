package org.example.projets2.dao;

import org.example.projets2.model.Utilisateur;
import java.sql.SQLException;
import java.util.List;

public interface UtilisateurDao {
    /**
     * Insère un nouvel utilisateur et met à jour son id auto-généré.
     */
    void create(Utilisateur u) throws SQLException;

    /**
     * Retourne l’utilisateur correspondant à cet email, ou null si aucun.
     */
    Utilisateur findByEmail(String email) throws SQLException;

    /**
     * Retourne l’utilisateur d’après son identifiant, ou null si absent.
     */
    Utilisateur findById(int id) throws SQLException;

    void delete(int id) throws SQLException;

    List<Utilisateur> findAll() throws SQLException;
}
