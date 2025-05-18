-- init_creneau.sql
CREATE TABLE IF NOT EXISTS Creneau (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  date TEXT NOT NULL,         -- stocké au format ISO (YYYY-MM-DD)
  debut TEXT NOT NULL,        -- stocké au format HH:MM
  fin TEXT NOT NULL,          -- stocké au format HH:MM
  cours_id INTEGER NOT NULL,
  salle_id INTEGER NOT NULL,
  FOREIGN KEY (cours_id) REFERENCES Cours(id),
  FOREIGN KEY (salle_id) REFERENCES Salle(id)
);