-- init_groupe_creneau.sql
CREATE TABLE IF NOT EXISTS Groupe_Creneau (
                                              groupe_id INTEGER NOT NULL,
                                              creneau_id INTEGER NOT NULL,
                                              PRIMARY KEY (groupe_id, creneau_id),
    FOREIGN KEY (groupe_id) REFERENCES Groupe(id),
    FOREIGN KEY (creneau_id) REFERENCES Creneau(id)
    );