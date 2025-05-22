package org.example.projets2.dao;

import org.example.projets2.model.Creneau;
import org.example.projets2.model.Groupe;
import org.example.projets2.model.Utilisateur;

import java.sql.SQLException;
import java.util.List;

/**
 * API de persistance pour les objets Groupe.
 */
public interface GroupeDao {

    /**
     * Insère un nouveau groupe et met à jour son ID auto-généré.
     */
    void create(Groupe groupe) throws SQLException;

    /**
     * Retourne le groupe d'après son identifiant, ou null si absent.
     */
    Groupe findById(int id) throws SQLException;

    /**
     * Retourne la liste de tous les groupes en base.
     */
    List<Groupe> findAll() throws SQLException;

    /**
     * Met à jour les informations d'un groupe existant.
     */
    void update(Groupe groupe) throws SQLException;

    /**
     * Supprime un groupe d'après son identifiant.
     */
    void delete(int id) throws SQLException;

    /**
     * Ajoute un étudiant à un groupe.
     */
    void addEtudiant(int groupeId, int etudiantId) throws SQLException;

    /**
     * Retire un étudiant d'un groupe.
     */
    void removeEtudiant(int groupeId, int etudiantId) throws SQLException;

    /**
     * Retourne la liste des étudiants d'un groupe.
     */
    List<Utilisateur> findEtudiantsByGroupe(int groupeId) throws SQLException;

    /**
     * Retourne la liste des groupes auxquels appartient un étudiant.
     */
    List<Groupe> findGroupesByEtudiant(int etudiantId) throws SQLException;

    /**
     * Associe un groupe à un créneau.
     */
    void addCreneau(int groupeId, int creneauId) throws SQLException;

    /**
     * Dissocie un groupe d'un créneau.
     */
    void removeCreneau(int groupeId, int creneauId) throws SQLException;

    /**
     * Retourne la liste des créneaux associés à un groupe.
     */
    List<Creneau> findCreneauxByGroupe(int groupeId) throws SQLException;

    /**
     * Retourne la liste des groupes associés à un créneau.
     */
    List<Groupe> findGroupesByCreneau(int creneauId) throws SQLException;
}