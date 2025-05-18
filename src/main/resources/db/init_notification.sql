-- init_notification.sql
CREATE TABLE IF NOT EXISTS Notification (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  titre TEXT NOT NULL,
  message TEXT NOT NULL,
  type TEXT NOT NULL,
  date_creation TEXT NOT NULL, -- stocké au format ISO (YYYY-MM-DD HH:MM:SS)
  lue INTEGER NOT NULL DEFAULT 0, -- booléen : 0 = non lue, 1 = lue
  destinataire_id INTEGER NOT NULL,
  creneau_id INTEGER,
  FOREIGN KEY (destinataire_id) REFERENCES Utilisateur(id),
  FOREIGN KEY (creneau_id) REFERENCES Creneau(id)
);