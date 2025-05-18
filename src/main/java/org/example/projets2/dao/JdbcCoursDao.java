package org.example.projets2.dao;

import org.example.projets2.model.Cours;
import org.example.projets2.model.Utilisateur;
import org.example.projets2.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation JDBC de CoursDao.
 */
public class JdbcCoursDao implements CoursDao {

    private static final String INSERT_SQL =
            "INSERT INTO Cours(nom, enseignant_id, duree) VALUES(?,?,?)";
    private static final String SELECT_BY_ID =
            "SELECT id, nom, enseignant_id, duree FROM Cours WHERE id = ?";
    private static final String SELECT_ALL =
            "SELECT id, nom, enseignant_id, duree FROM Cours";
    private static final String UPDATE_SQL =
            "UPDATE Cours SET nom = ?, enseignant_id = ?, duree = ? WHERE id = ?";
    private static final String DELETE_SQL =
            "DELETE FROM Cours WHERE id = ?";

    private final UtilisateurDao utilisateurDao = new JdbcUtilisateurDao();

    @Override
    public void create(Cours c) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getNom());
            ps.setInt(2, c.getEnseignant().getId());
            ps.setInt(3, c.getDuree());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public Cours findById(int id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cours c = new Cours();
                    c.setId(rs.getInt("id"));
                    c.setNom(rs.getString("nom"));
                    // Chargement de l'enseignant via UtilisateurDao
                    int enseignantId = rs.getInt("enseignant_id");
                    Utilisateur u = utilisateurDao.findById(enseignantId);
                    c.setEnseignant(u);
                    c.setDuree(rs.getInt("duree"));
                    return c;
                }
                return null;
            }
        }
    }

    @Override
    public List<Cours> findAll() throws SQLException {
        List<Cours> liste = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Cours c = new Cours();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                int enseignantId = rs.getInt("enseignant_id");
                Utilisateur u = utilisateurDao.findById(enseignantId);
                c.setEnseignant(u);
                c.setDuree(rs.getInt("duree"));
                liste.add(c);
            }
        }
        return liste;
    }

    @Override
    public void update(Cours c) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {

            ps.setString(1, c.getNom());
            ps.setInt(2, c.getEnseignant().getId());
            ps.setInt(3, c.getDuree());
            ps.setInt(4, c.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}