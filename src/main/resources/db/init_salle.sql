-- init_salle.sql
CREATE TABLE IF NOT EXISTS Salle (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nom TEXT NOT NULL,
  capacite INTEGER NOT NULL,
  equipements TEXT
);