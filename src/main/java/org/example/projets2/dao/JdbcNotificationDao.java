package org.example.projets2.dao;

import org.example.projets2.model.Creneau;
import org.example.projets2.model.Notification;
import org.example.projets2.model.Utilisateur;
import org.example.projets2.util.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation JDBC de NotificationDao.
 */
public class JdbcNotificationDao implements NotificationDao {

    private static final String INSERT_SQL =
            "INSERT INTO Notification(titre, message, type, date_creation, lue, destinataire_id, creneau_id) " +
                    "VALUES(?,?,?,?,?,?,?)";
    private static final String SELECT_BY_ID =
            "SELECT id, titre, message, type, date_creation, lue, destinataire_id, creneau_id " +
                    "FROM Notification WHERE id = ?";
    private static final String SELECT_ALL =
            "SELECT id, titre, message, type, date_creation, lue, destinataire_id, creneau_id " +
                    "FROM Notification";
    private static final String SELECT_BY_DESTINATAIRE =
            "SELECT id, titre, message, type, date_creation, lue, destinataire_id, creneau_id " +
                    "FROM Notification WHERE destinataire_id = ? ORDER BY date_creation DESC";
    private static final String SELECT_NON_LUES_BY_DESTINATAIRE =
            "SELECT id, titre, message, type, date_creation, lue, destinataire_id, creneau_id " +
                    "FROM Notification WHERE destinataire_id = ? AND lue = 0 ORDER BY date_creation DESC";
    private static final String UPDATE_SQL =
            "UPDATE Notification SET titre = ?, message = ?, type = ?, date_creation = ?, lue = ?, " +
                    "destinataire_id = ?, creneau_id = ? WHERE id = ?";
    private static final String MARK_AS_READ_SQL =
            "UPDATE Notification SET lue = 1 WHERE id = ?";
    private static final String DELETE_SQL =
            "DELETE FROM Notification WHERE id = ?";

    private final UtilisateurDao utilisateurDao = new JdbcUtilisateurDao();
    private final CreneauDao creneauDao = new JdbcCreneauDao();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void create(Notification notification) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, notification.getTitre());
            ps.setString(2, notification.getMessage());
            ps.setString(3, notification.getType().name());
            ps.setString(4, notification.getDateCreation().format(formatter));
            ps.setInt(5, notification.isLue() ? 1 : 0);
            ps.setInt(6, notification.getDestinataire().getId());

            // Créneau optionnel
            if (notification.getCreneau() != null) {
                ps.setInt(7, notification.getCreneau().getId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    notification.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public Notification findById(int id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToNotification(rs);
                }
                return null;
            }
        }
    }

    @Override
    public List<Notification> findAll() throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                notifications.add(mapRowToNotification(rs));
            }
        }
        return notifications;
    }

    @Override
    public List<Notification> findByDestinataire(int destinataireId) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_DESTINATAIRE)) {

            ps.setInt(1, destinataireId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapRowToNotification(rs));
                }
            }
        }
        return notifications;
    }

    @Override
    public List<Notification> findNonLuesByDestinataire(int destinataireId) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_NON_LUES_BY_DESTINATAIRE)) {

            ps.setInt(1, destinataireId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapRowToNotification(rs));
                }
            }
        }
        return notifications;
    }

    @Override
    public void update(Notification notification) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {

            ps.setString(1, notification.getTitre());
            ps.setString(2, notification.getMessage());
            ps.setString(3, notification.getType().name());
            ps.setString(4, notification.getDateCreation().format(formatter));
            ps.setInt(5, notification.isLue() ? 1 : 0);
            ps.setInt(6, notification.getDestinataire().getId());

            // Créneau optionnel
            if (notification.getCreneau() != null) {
                ps.setInt(7, notification.getCreneau().getId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }

            ps.setInt(8, notification.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void markAsRead(int id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(MARK_AS_READ_SQL)) {

            ps.setInt(1, id);
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
     * Convertit une ligne de résultat SQL en objet Notification.
     */
    private Notification mapRowToNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId(rs.getInt("id"));
        notification.setTitre(rs.getString("titre"));
        notification.setMessage(rs.getString("message"));
        notification.setType(Notification.Type.valueOf(rs.getString("type")));
        notification.setDateCreation(LocalDateTime.parse(rs.getString("date_creation"), formatter));
        notification.setLue(rs.getInt("lue") == 1);

        // Récupérer le destinataire
        int destinataireId = rs.getInt("destinataire_id");
        Utilisateur destinataire = utilisateurDao.findById(destinataireId);
        notification.setDestinataire(destinataire);

        // Récupérer le créneau (s'il existe)
        int creneauId = rs.getInt("creneau_id");
        if (!rs.wasNull()) {
            Creneau creneau = creneauDao.findById(creneauId);
            notification.setCreneau(creneau);
        }

        return notification;
    }
}