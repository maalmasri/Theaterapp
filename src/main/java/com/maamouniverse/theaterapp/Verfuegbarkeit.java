package com.maamouniverse.theaterapp;

import java.time.LocalDate;

public class Verfuegbarkeit {
    private Long id;
    private User mitarbeiter;    // Wer gibt die Dispo ab?
    private LocalDate datum;     // Für welchen Tag gilt die Dispo?
    private boolean kannArbeiten; // true = "kann arbeiten", false = "kann nicht arbeiten"

    // Der Konstruktor, um ein neues Dispo-Element im Speicher anzulegen
    public Verfuegbarkeit(Long id, User mitarbeiter, LocalDate datum, boolean kannArbeiten) {
        this.id = id;
        this.mitarbeiter = mitarbeiter;
        this.datum = datum;
        this.kannArbeiten = kannArbeiten;
    }

    // --- GETTER & SETTER ---
    // Sie werden vom DienstPlanerService genutzt, um die Daten zu lesen

    public Long getId() {
        return id;
    }

    public User getMitarbeiter() {
        return mitarbeiter;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public boolean isKannArbeiten() {
        return kannArbeiten;
    }

    public void setKannArbeiten(boolean kannArbeiten) {
        this.kannArbeiten = kannArbeiten;
    }
}