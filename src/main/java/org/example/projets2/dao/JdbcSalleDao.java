package org.example.projets2.dao;

import org.example.projets2.model.Salle;
import org.example.projets2.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation JDBC de SalleDao.
 */
public class JdbcSalleDao implements SalleDao {

    private static final String INSERT_SQL =
            "INSERT INTO Salle(nom, capacite, equipements) VALUES(?,?,?)";
    private static final String SELECT_BY_ID =
            "SELECT id, nom, capacite, equipements FROM Salle WHERE id = ?";
    private static final String SELECT_ALL =
            "SELECT id, nom, capacite, equipements FROM Salle";
    private static final String UPDATE_SQL =
            "UPDATE Salle SET nom = ?, capacite = ?, equipements = ? WHERE id = ?";
    private static final String DELETE_SQL =
            "DELETE FROM Salle WHERE id = ?";

    @Override
    public void create(Salle s) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            // 1) Lier les paramètres
            ps.setString(1, s.getNom());
            ps.setInt(2, s.getCapacite());
            ps.setString(3, s.getEquipements());

            // 2) Exécuter l'insertion
            ps.executeUpdate();

            // 3) Récupérer l'id auto-généré
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    s.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public Salle findById(int id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Construire et renvoyer la Salle
                    Salle s = new Salle();
                    s.setId(rs.getInt("id"));
                    s.setNom(rs.getString("nom"));
                    s.setCapacite(rs.getInt("capacite"));
                    s.setEquipements(rs.getString("equipements"));
                    return s;
                }
                return null;
            }
        }
    }

    @Override
    public List<Salle> findAll() throws SQLException {
        List<Salle> salles = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Salle s = new Salle();
                s.setId(rs.getInt("id"));
                s.setNom(rs.getString("nom"));
                s.setCapacite(rs.getInt("capacite"));
                s.setEquipements(rs.getString("equipements"));
                salles.add(s);
            }
        }
        return salles;
    }

    @Override
    public void update(Salle s) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {

            // Lier les nouveaux champs
            ps.setString(1, s.getNom());
            ps.setInt(2, s.getCapacite());
            ps.setString(3, s.getEquipements());
            ps.setInt(4, s.getId());

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