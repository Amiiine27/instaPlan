-- init_cours.sql
CREATE TABLE IF NOT EXISTS Cours (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nom TEXT NOT NULL,
  enseignant_id INTEGER NOT NULL,
  duree INTEGER NOT NULL,
  FOREIGN KEY (enseignant_id) REFERENCES Utilisateur(id)
);