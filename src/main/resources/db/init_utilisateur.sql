-- init_utilisateur.sql
-- Création de la table 'Utilisateur' conformément au diagramme de classe UML

CREATE TABLE IF NOT EXISTS Utilisateur (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  lastName TEXT NOT NULL,
  firstName TEXT NOT NULL,
  email TEXT UNIQUE NOT NULL,
  password TEXT NOT NULL
);