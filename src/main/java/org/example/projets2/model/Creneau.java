package org.example.projets2.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Creneau {
    private int id;
    private LocalDate date;
    private LocalTime debut;
    private LocalTime fin;
    private Cours cours;
    private Salle salle;

    public Creneau() {}

    public Creneau(LocalDate date, LocalTime debut, LocalTime fin,
                   Cours cours, Salle salle) {
        this.date = date;
        this.debut = debut;
        this.fin = fin;
        this.cours = cours;
        this.salle = salle;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getDebut() { return debut; }
    public void setDebut(LocalTime debut) { this.debut = debut; }

    public LocalTime getFin() { return fin; }
    public void setFin(LocalTime fin) { this.fin = fin; }

    public Cours getCours() { return cours; }
    public void setCours(Cours cours) { this.cours = cours; }

    public Salle getSalle() { return salle; }
    public void setSalle(Salle salle) { this.salle = salle; }
}