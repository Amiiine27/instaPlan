package org.example.projets2.dao;

import org.example.projets2.model.Creneau;
import org.example.projets2.model.Groupe;
import org.example.projets2.model.Utilisateur;
import org.example.projets2.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation JDBC de GroupeDao.
 */
public class JdbcGroupeDao implements GroupeDao {

    private static final String INSERT_SQL =
            "INSERT INTO Groupe(nom, description) VALUES(?,?)";
    private static final String SELECT_BY_ID =
            "SELECT id, nom, description FROM Groupe WHERE id = ?";
    private static final String SELECT_ALL =
            "SELECT id, nom, description FROM Groupe";
    private static final String UPDATE_SQL =
            "UPDATE Groupe SET nom = ?, description = ? WHERE id = ?";
    private static final String DELETE_SQL =
            "DELETE FROM Groupe WHERE id = ?";

    // Requêtes pour la table Groupe_Etudiant
    private static final String INSERT_ETUDIANT_SQL =
            "INSERT INTO Groupe_Etudiant(groupe_id, etudiant_id) VALUES(?,?)";
    private static final String DELETE_ETUDIANT_SQL =
            "DELETE FROM Groupe_Etudiant WHERE groupe_id = ? AND etudiant_id = ?";
    private static final String SELECT_ETUDIANTS_BY_GROUPE =
            "SELECT etudiant_id FROM Groupe_Etudiant WHERE groupe_id = ?";
    private static final String SELECT_GROUPES_BY_ETUDIANT =
            "SELECT groupe_id FROM Groupe_Etudiant WHERE etudiant_id = ?";

    // Requêtes pour la table Groupe_Creneau
    private static final String INSERT_CRENEAU_SQL =
            "INSERT INTO Groupe_Creneau(groupe_id, creneau_id) VALUES(?,?)";
    private static final String DELETE_CRENEAU_SQL =
            "DELETE FROM Groupe_Creneau WHERE groupe_id = ? AND creneau_id = ?";
    private static final String SELECT_CRENEAUX_BY_GROUPE =
            "SELECT creneau_id FROM Groupe_Creneau WHERE groupe_id = ?";
    private static final String SELECT_GROUPES_BY_CRENEAU =
            "SELECT groupe_id FROM Groupe_Creneau WHERE creneau_id = ?";

    private final UtilisateurDao utilisateurDao = new JdbcUtilisateurDao();
    private final CreneauDao creneauDao = new JdbcCreneauDao();

    @Override
    public void create(Groupe groupe) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, groupe.getNom());
            ps.setString(2, groupe.getDescription());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    groupe.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public Groupe findById(int id) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Groupe groupe = new Groupe();
                    groupe.setId(rs.getInt("id"));
                    groupe.setNom(rs.getString("nom"));
                    groupe.setDescription(rs.getString("description"));

                    // Charger les étudiants du groupe
                    List<Utilisateur> etudiants = findEtudiantsByGroupe(id);
                    groupe.setEtudiants(etudiants);

                    return groupe;
                }
                return null;
            }
        }
    }

    @Override
    public List<Groupe> findAll() throws SQLException {
        List<Groupe> groupes = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Groupe groupe = new Groupe();
                groupe.setId(rs.getInt("id"));
                groupe.setNom(rs.getString("nom"));
                groupe.setDescription(rs.getString("description"));

                // Charger les étudiants du groupe
                List<Utilisateur> etudiants = findEtudiantsByGroupe(groupe.getId());
                groupe.setEtudiants(etudiants);

                groupes.add(groupe);
            }
        }
        return groupes;
    }

    @Override
    public void update(Groupe groupe) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {

            ps.setString(1, groupe.getNom());
            ps.setString(2, groupe.getDescription());
            ps.setInt(3, groupe.getId());
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

    @Override
    public void addEtudiant(int groupeId, int etudiantId) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_ETUDIANT_SQL)) {

            ps.setInt(1, groupeId);
            ps.setInt(2, etudiantId);
            ps.executeUpdate();
        }
    }

    @Override
    public void removeEtudiant(int groupeId, int etudiantId) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_ETUDIANT_SQL)) {

            ps.setInt(1, groupeId);
            ps.setInt(2, etudiantId);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Utilisateur> findEtudiantsByGroupe(int groupeId) throws SQLException {
        List<Utilisateur> etudiants = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ETUDIANTS_BY_GROUPE)) {

            ps.setInt(1, groupeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int etudiantId = rs.getInt("etudiant_id");
                    Utilisateur etudiant = utilisateurDao.findById(etudiantId);
                    if (etudiant != null) {
                        etudiants.add(etudiant);
                    }
                }
            }
        }
        return etudiants;
    }

    @Override
    public List<Groupe> findGroupesByEtudiant(int etudiantId) throws SQLException {
        List<Groupe> groupes = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_GROUPES_BY_ETUDIANT)) {

            ps.setInt(1, etudiantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int groupeId = rs.getInt("groupe_id");
                    Groupe groupe = findById(groupeId);
                    if (groupe != null) {
                        groupes.add(groupe);
                    }
                }
            }
        }
        return groupes;
    }

    @Override
    public void addCreneau(int groupeId, int creneauId) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_CRENEAU_SQL)) {

            ps.setInt(1, groupeId);
            ps.setInt(2, creneauId);
            ps.executeUpdate();
        }
    }

    @Override
    public void removeCreneau(int groupeId, int creneauId) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_CRENEAU_SQL)) {

            ps.setInt(1, groupeId);
            ps.setInt(2, creneauId);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Creneau> findCreneauxByGroupe(int groupeId) throws SQLException {
        List<Creneau> creneaux = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_CRENEAUX_BY_GROUPE)) {

            ps.setInt(1, groupeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int creneauId = rs.getInt("creneau_id");
                    Creneau creneau = creneauDao.findById(creneauId);
                    if (creneau != null) {
                        creneaux.add(creneau);
                    }
                }
            }
        }
        return creneaux;
    }

    @Override
    public List<Groupe> findGroupesByCreneau(int creneauId) throws SQLException {
        List<Groupe> groupes = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_GROUPES_BY_CRENEAU)) {

            ps.setInt(1, creneauId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int groupeId = rs.getInt("groupe_id");
                    Groupe groupe = findById(groupeId);
                    if (groupe != null) {
                        groupes.add(groupe);
                    }
                }
            }
        }
        return groupes;
    }
}