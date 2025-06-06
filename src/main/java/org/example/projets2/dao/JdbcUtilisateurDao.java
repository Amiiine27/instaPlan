package org.example.projets2.dao;

import org.example.projets2.model.Role;
import org.example.projets2.model.Utilisateur;
import org.example.projets2.util.Database;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcUtilisateurDao implements UtilisateurDao {

    private static final String INSERT_SQL =
            "INSERT INTO Utilisateur(lastName, firstName, email, password, role) VALUES(?,?,?,?,?)";
    private static final String SELECT_BY_EMAIL =
            "SELECT id, lastName, firstName, email, password, role FROM Utilisateur WHERE email = ?";
    private static final String SELECT_BY_ID =
            "SELECT id, lastName, firstName, email, password, role FROM Utilisateur WHERE id = ?";
    private static final String DELETE_SQL =
            "DELETE FROM Utilisateur WHERE id = ?";
    private static final String SELECT_ALL =
            "SELECT id, lastName, firstName, email, password, role FROM Utilisateur";

    @Override
    public void create(Utilisateur u) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.getLastName());
            ps.setString(2, u.getFirstName());
            ps.setString(3, u.getEmail());
            // Hachage du mot de passe avant insertion
            String hashedPassword = BCrypt.hashpw(u.getPassword(), BCrypt.gensalt());
            ps.setString(4, hashedPassword);
            // Ajout du rôle (stocké comme le nom de l'enum)
            ps.setString(5, u.getRole().name());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    u.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public Utilisateur findByEmail(String email) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_EMAIL)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Utilisateur u = new Utilisateur();
                    u.setId(rs.getInt("id"));
                    u.setLastName(rs.getString("lastName"));
                    u.setFirstName(rs.getString("firstName"));
                    u.setEmail(rs.getString("email"));
                    u.setPassword(rs.getString("password"));
                    // Récupérer le rôle et le convertir en enum
                    String roleStr = rs.getString("role");
                    u.setRole(Role.valueOf(roleStr));
                    return u;
                }
                return null;
            }
        }
    }

    @Override
    public Utilisateur findById(int id) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Utilisateur u = new Utilisateur();
                    u.setId(rs.getInt("id"));
                    u.setLastName(rs.getString("lastName"));
                    u.setFirstName(rs.getString("firstName"));
                    u.setEmail(rs.getString("email"));
                    u.setPassword(rs.getString("password"));
                    // Récupérer le rôle et le convertir en enum
                    String roleStr = rs.getString("role");
                    u.setRole(Role.valueOf(roleStr));
                    return u;
                }
                return null;
            }
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Utilisateur> findAll() throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Utilisateur u = new Utilisateur();
                u.setId(rs.getInt("id"));
                u.setLastName(rs.getString("lastName"));
                u.setFirstName(rs.getString("firstName"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));

                // Récupérer le rôle et le convertir en enum
                String roleStr = rs.getString("role");
                u.setRole(Role.valueOf(roleStr));

                utilisateurs.add(u);
            }
        }

        return utilisateurs;
    }
}