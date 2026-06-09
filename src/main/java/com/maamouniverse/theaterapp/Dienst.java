package com.maamouniverse.theaterapp;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class Dienst {
	private Long dienstId;
	private User user;
	private Vorstellung vorstellung;
	private DienstTyp dienstTyp;
	private LocalDateTime geplanterArbeitsbeginn; // Automatisch: Vorstellung minus 1h oder 1.5h
    private LocalDateTime geplantesArbeitsende;
    private LocalDateTime tatsaechlichesEndzeit; // Vom Mitarbeiter eingetragen
    private BigDecimal berechneterVerdienst;
	
    private boolean istBlumenDienst;
    private int anzahlBlumen;
    
    public Dienst(Long id, User user, Vorstellung vorstellung, DienstTyp dienstTyp, LocalDateTime geplanterArbeitsbeginn, LocalDateTime geplantesArbeitsende) {
        this.dienstId = id;
        this.user = user;
        this.vorstellung = vorstellung;
        this.dienstTyp = dienstTyp;
        this.geplanterArbeitsbeginn = geplanterArbeitsbeginn;
        this.geplantesArbeitsende = geplantesArbeitsende;
        this.berechneterVerdienst = BigDecimal.ZERO;
        this.istBlumenDienst=false;
        this.anzahlBlumen=0;
        
	}
    
    public boolean istBlumenDienst() {
    	return istBlumenDienst;
    }
    public void setIstBlumenDienst(boolean istBlumenDienst) {
    	this.istBlumenDienst=istBlumenDienst;
    }
    public int getAnzahleBlumen() {
    	return anzahlBlumen;
    }
    public void setAnzahlBlumen(int anzahlBlumen) {
    	this.anzahlBlumen=anzahlBlumen;
    }

    public User getMitarbeiter() {
    		return user;
    		
    }

    public Vorstellung getVorstellung() {
    		return vorstellung;
    		
    }

    public DienstTyp getDienstTyp() {
    		return dienstTyp;
    		
    }

    public LocalDateTime getGeplanterArbeitsbeginn() {
    		return geplanterArbeitsbeginn;
    		
    }

    public LocalDateTime getGeplantesArbeitsende() {
    		return geplantesArbeitsende;
    		
    }

    public LocalDateTime getTatsaechlichesArbeitsende() {
    		return tatsaechlichesEndzeit;
    		
    }

    public void setTatsaechlichesArbeitsende(LocalDateTime ende) {
    		this.tatsaechlichesEndzeit = ende;
    		
    }

    public BigDecimal getVerdienst() {
    		return berechneterVerdienst;
    		
    }

    public void setVerdienst(BigDecimal verdienst) {
    		this.berechneterVerdienst = verdienst;
    		
    }
    
    public Long getDienstId() {
    	return dienstId;
    }
    public void setDienstId(Long dienstId) {
    	this.dienstId=dienstId;
    }

}
