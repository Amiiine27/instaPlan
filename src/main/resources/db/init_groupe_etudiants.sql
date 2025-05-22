-- init_groupe_etudiant.sql
CREATE TABLE IF NOT EXISTS Groupe_Etudiant (
                                               groupe_id INTEGER NOT NULL,
                                               etudiant_id INTEGER NOT NULL,
                                               PRIMARY KEY (groupe_id, etudiant_id),
    FOREIGN KEY (groupe_id) REFERENCES Groupe(id),
    FOREIGN KEY (etudiant_id) REFERENCES Utilisateur(id)
    );