package org.example.projets2.dao;

import org.example.projets2.model.Creneau;
import org.example.projets2.model.Cours;
import org.example.projets2.model.Salle;
import org.example.projets2.util.Database;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation JDBC de CreneauDao.
 */
public class JdbcCreneauDao implements CreneauDao {

    private static final String INSERT_SQL =
            "INSERT INTO Creneau(date, debut, fin, cours_id, salle_id) VALUES(?,?,?,?,?)";
    private static final String SELECT_BY_ID =
            "SELECT id, date, debut, fin, cours_id, salle_id FROM Creneau WHERE id = ?";
    private static final String SELECT_ALL =
            "SELECT id, date, debut, fin, cours_id, salle_id FROM Creneau";
    private static final String SELECT_BY_COURS =
            "SELECT id, date, debut, fin, cours_id, salle_id FROM Creneau WHERE cours_id = ?";
    private static final String SELECT_BY_SALLE =
            "SELECT id, date, debut, fin, cours_id, salle_id FROM Creneau WHERE salle_id = ?";
    private static final String UPDATE_SQL =
            "UPDATE Creneau SET date = ?, debut = ?, fin = ?, cours_id = ?, salle_id = ? WHERE id = ?";
    private static final String DELETE_SQL =
            "DELETE FROM Creneau WHERE id = ?";

    private final CoursDao coursDao = new JdbcCoursDao();
    private final SalleDao salleDao = new JdbcSalleDao();

    @Override
    public void create(Creneau c) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getDate().toString());
            ps.setString(2, c.getDebut().toString());
            ps.setString(3, c.getFin().toString());
            ps.setInt(4, c.getCours().getId());
            ps.setInt(5, c.getSalle().getId());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public Creneau findById(int id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToCreneau(rs);
                }
                return null;
            }
        }
    }

    @Override
    public List<Creneau> findAll() throws SQLException {
        List<Creneau> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToCreneau(rs));
            }
        }
        return list;
    }

    @Override
    public List<Creneau> findByCours(int coursId) throws SQLException {
        List<Creneau> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_COURS)) {

            ps.setInt(1, coursId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToCreneau(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Creneau> findBySalle(int salleId) throws SQLException {
        List<Creneau> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_SALLE)) {

            ps.setInt(1, salleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToCreneau(rs));
                }
            }
        }
        return list;
    }

    @Override
    public void update(Creneau c) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {

            ps.setString(1, c.getDate().toString());
            ps.setString(2, c.getDebut().toString());
            ps.setString(3, c.getFin().toString());
            ps.setInt(4, c.getCours().getId());
            ps.setInt(5, c.getSalle().getId());
            ps.setInt(6, c.getId());
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

    /**
     * Transforme la ligne courante de ResultSet en objet Creneau complet.
     */
    private Creneau mapRowToCreneau(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        LocalDate date = LocalDate.parse(rs.getString("date"));
        LocalTime debut = LocalTime.parse(rs.getString("debut"));
        LocalTime fin = LocalTime.parse(rs.getString("fin"));

        // Chargement des objets Cours et Salle
        Cours cours = coursDao.findById(rs.getInt("cours_id"));
        Salle salle = salleDao.findById(rs.getInt("salle_id"));

        Creneau c = new Creneau();
        c.setId(id);
        c.setDate(date);
        c.setDebut(debut);
        c.setFin(fin);
        c.setCours(cours);
        c.setSalle(salle);
        return c;
    }
}