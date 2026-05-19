package com.maamouniverse.theaterapp;

import java.time.LocalDateTime;

public class Vorstellung {
	private Long vorstellungsId;
	private String titel;
    private LocalDateTime beginnVorstellung; // Datum + Uhrzeit
    private boolean istAbgesagt;
    private LocalDateTime absageZeitpunkt; // Wird gefüllt, wenn Admin absagt
	private Standort standort;
    
    public Vorstellung(Long vorstellungsid, String titel, LocalDateTime beginnVorstellung,Standort standort) {
    		this.vorstellungsId=vorstellungsid;
    		this.titel=titel;
    		this.beginnVorstellung=beginnVorstellung;
    		this.standort = standort;
    		this.istAbgesagt = false;
    }
    public void absagen(LocalDateTime zeitpunkt) {
    	this.istAbgesagt = true;
    	this.absageZeitpunkt=zeitpunkt;
    	
    }
   
    public Long getId() {
    		return vorstellungsId;
    	
    }
    
    public String getTitel() {
    		return titel;
    		
    }
    
    public LocalDateTime getBeginnVorstellung() {
    		return beginnVorstellung;
    		
    }
    
    public boolean isIstAbgesagt() {
    	return istAbgesagt;
    	
    }
    
    public LocalDateTime getAbsageZeitpunkt() {
    		return absageZeitpunkt;
    		
    }
    
    public Standort getStandort() {
		return standort;
		
    }
    public void setStandort(Standort standort) {
    	this.standort=standort;
    }
    

}
