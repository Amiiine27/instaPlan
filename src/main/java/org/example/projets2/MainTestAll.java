package org.example.projets2;

import org.example.projets2.dao.*;
import org.example.projets2.model.*;
import org.example.projets2.util.DatabaseInitializer;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class MainTestAll {

    public static void main(String[] args) {
        // 0) Initialisation de la base
        DatabaseInitializer.initialize();

        // ── Test Utilisateur ───────────────────────────────────────────────────────
        UtilisateurDao userDao = new JdbcUtilisateurDao();
        Utilisateur user = new Utilisateur("Doe", "John", "secret");
        try {
            userDao.create(user);
            System.out.println("👤 Utilisateur créé: ID=" + user.getId()
                    + ", email=" + user.getEmail());
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // ── Test Cours ───────────────────────────────────────────────────────────
        CoursDao coursDao = new JdbcCoursDao();
        Cours cours = new Cours("Mathématiques", user, 90);
        try {
            coursDao.create(cours);
            System.out.println("📚 Cours créé: ID=" + cours.getId()
                    + ", nom=" + cours.getNom()
                    + ", enseignant=" + cours.getEnseignant().getFirstName());
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // Lire et lister
        try {
            Cours c1 = coursDao.findById(cours.getId());
            System.out.println("🔍 findById Cours: " + c1.getNom());

            List<Cours> allCours = coursDao.findAll();
            System.out.println("📋 findAll Cours (" + allCours.size() + "):");
            allCours.forEach(c -> System.out.println("   - " + c.getId() + ": " + c.getNom()));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // ── Test Salle ───────────────────────────────────────────────────────────
        SalleDao salleDao = new JdbcSalleDao();
        Salle salle = new Salle("A101", 30, "Projecteur,Clim");
        try {
            salleDao.create(salle);
            System.out.println("🏫 Salle créée: ID=" + salle.getId()
                    + ", nom=" + salle.getNom());
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        try {
            Salle s1 = salleDao.findById(salle.getId());
            System.out.println("🔍 findById Salle: " + s1.getNom());

            List<Salle> allSalles = salleDao.findAll();
            System.out.println("📋 findAll Salles (" + allSalles.size() + "):");
            allSalles.forEach(s -> System.out.println("   - " + s.getId() + ": " + s.getNom()));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // ── Test Créneau ────────────────────────────────────────────────────────
        CreneauDao creneauDao = new JdbcCreneauDao();
        Creneau cr = new Creneau(
                LocalDate.now(),
                LocalTime.of(9, 0),
                LocalTime.of(10, 30),
                cours,
                salle
        );
        try {
            creneauDao.create(cr);
            System.out.println("⏰ Créneau créé: ID=" + cr.getId()
                    + ", date=" + cr.getDate()
                    + ", cours=" + cr.getCours().getNom()
                    + ", salle=" + cr.getSalle().getNom());
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        try {
            Creneau r1 = creneauDao.findById(cr.getId());
            System.out.println("🔍 findById Créneau: " + r1.getDate() + " " + r1.getDebut());

            List<Creneau> byCours = creneauDao.findByCours(cours.getId());
            System.out.println("📋 findByCours (" + byCours.size() + "):");
            byCours.forEach(c -> System.out.println("   - ID=" + c.getId() + ", salle=" + c.getSalle().getNom()));

            List<Creneau> bySalle = creneauDao.findBySalle(salle.getId());
            System.out.println("📋 findBySalle (" + bySalle.size() + "):");
            bySalle.forEach(c -> System.out.println("   - ID=" + c.getId() + ", cours=" + c.getCours().getNom()));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // ── Clean-up (optionnel) ─────────────────────────────────────────────────
        try {
            creneauDao.delete(cr.getId());
            coursDao.delete(cours.getId());
            salleDao.delete(salle.getId());
            userDao.delete(user.getId());
            System.out.println("🗑️ Tous les objets test supprimés.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}