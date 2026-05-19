package com.maamouniverse.theaterapp;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class AbrechnungsService {

    // Tarife für SCHLIESSER
    private static final BigDecimal PAUSCHALE_KURZ_SCHLIESSER = new BigDecimal("25.00");
    private static final BigDecimal STUNDENLOHN_BASIS_SCHLIESSER = new BigDecimal("14.00"); // Bis 8 Std. 
    private static final BigDecimal UEBERSTUNDE_HALBE_SCHLIESSER = new BigDecimal("8.50");  // Ab 8 Std. pro halbe Std. 

    // Fixe Tarife für OBERSCHLIESSER
    private static final BigDecimal PAUSCHALE_KURZ_OBERSCHLIESSER = new BigDecimal("50.00");
    private static final BigDecimal PAUSCHALE_LANG_OBERSCHLIESSER = new BigDecimal("80.00");
    private static final BigDecimal PAUSCHALE_PREMIERE_OBERSCHLIESSER = new BigDecimal("120.00");
    
    private static final BigDecimal MAX_MINIJOB_GRENZE = new BigDecimal("556.00");
    
    
    public LocalDateTime berechneDienstbeginn(Vorstellung vorstellung, UserRole rolle) {
        if (rolle == UserRole.OBERSCHLIESSER) {
            return vorstellung.getBeginnVorstellung().minusMinutes(90);
        } else {
            return vorstellung.getBeginnVorstellung().minusMinutes(60);
        }
    }
    
    
    
    
    
    

    /**Verdienstberechnung*/
    public BigDecimal berechneVerdienst(Dienst dienst) {
        Vorstellung vorstellung = dienst.getVorstellung();
        User mitarbeiter = dienst.getMitarbeiter();

        if (vorstellung.isIstAbgesagt()) {
            long stundenVorher = ChronoUnit.HOURS.between(vorstellung.getAbsageZeitpunkt(),vorstellung.getBeginnVorstellung());
            if (stundenVorher >= 24) {
            	return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            } else {
                // Spätabsage: Mitarbeiter erhält den geplanten Fixbetrag / die geplante Zeit
                return berechneGeplantenVerdienstFuerAusfall(dienst);
            }
        }
        if (mitarbeiter.getRole() == UserRole.OBERSCHLIESSER) {
            switch (dienst.getDienstTyp()) {
                case KURZ:
                    return PAUSCHALE_KURZ_OBERSCHLIESSER.setScale(2, RoundingMode.HALF_UP);
                case LANG:
                    return PAUSCHALE_LANG_OBERSCHLIESSER.setScale(2, RoundingMode.HALF_UP);
                case PREMIERE:
                    return PAUSCHALE_PREMIERE_OBERSCHLIESSER.setScale(2, RoundingMode.HALF_UP);
                default:
                    return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }
        }        
        if (dienst.getDienstTyp() == DienstTyp.KURZ) {
            return PAUSCHALE_KURZ_SCHLIESSER.setScale(2, RoundingMode.HALF_UP); 
        }
        return berechneLangenDienstSchliesser(dienst.getGeplanterArbeitsbeginn(), dienst.getTatsaechlichesArbeitsende());
    }

    /** Berechnet den langen Dienst für Schließer */
    private BigDecimal berechneLangenDienstSchliesser(LocalDateTime start, LocalDateTime ende) {
        if (start == null || ende == null) {
        	return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        long gesamtMinuten = Duration.between(start, ende).toMinutes();
        long volleStunden = gesamtMinuten / 60;
        long restMinuten = gesamtMinuten % 60;

        BigDecimal abrechnungsStunden = BigDecimal.valueOf(volleStunden);

        // Die exakte "+1 Minute"-Rundung am Ende auf halbe/volle Stunden
        if (restMinuten >= 1 && restMinuten <= 30) {
            abrechnungsStunden = abrechnungsStunden.add(new BigDecimal("0.5"));
        } else if (restMinuten > 30) {
            abrechnungsStunden = abrechnungsStunden.add(new BigDecimal("1.0"));
        }
        BigDecimal maximalBasisStunden = new BigDecimal("8.0");
        if (abrechnungsStunden.compareTo(maximalBasisStunden) <= 0) {
            // Normaler Tarif für Schließer bis 8 Stunden
            return abrechnungsStunden.multiply(STUNDENLOHN_BASIS_SCHLIESSER).setScale(2, RoundingMode.HALF_UP); 
        } else {
            // Überlängentarif für Schließer ab 8 Stunden
            BigDecimal basisVerdienst = maximalBasisStunden.multiply(STUNDENLOHN_BASIS_SCHLIESSER); 
            BigDecimal ueberstunden = abrechnungsStunden.subtract(maximalBasisStunden);
            BigDecimal anzahlHalbeStunden = ueberstunden.multiply(new BigDecimal("2.0"));
            BigDecimal ueberstundenVerdienst = anzahlHalbeStunden.multiply(UEBERSTUNDE_HALBE_SCHLIESSER); 
            
            return basisVerdienst.add(ueberstundenVerdienst).setScale(2, RoundingMode.HALF_UP);
        }
    }
    /** Spätabsagen (< 24h)*/
    private BigDecimal berechneGeplantenVerdienstFuerAusfall(Dienst dienst) {
        User mitarbeiter = dienst.getMitarbeiter();
        
        // Wenn Oberschließer, kriegt er trotz Ausfall seine volle Pauschale
        if (mitarbeiter.getRole() == UserRole.OBERSCHLIESSER) {
            switch (dienst.getDienstTyp()) {
                case KURZ: return PAUSCHALE_KURZ_OBERSCHLIESSER;
                case LANG: return PAUSCHALE_LANG_OBERSCHLIESSER;
                case PREMIERE: return PAUSCHALE_PREMIERE_OBERSCHLIESSER;
            }
        }
        
        // Wenn normaler Schließer, berechne anhand der geplanten Zeiten
        if (dienst.getDienstTyp() == DienstTyp.KURZ) {
            return PAUSCHALE_KURZ_SCHLIESSER;
        }
        return berechneLangenDienstSchliesser(dienst.getGeplanterArbeitsbeginn(), dienst.getGeplantesArbeitsende());
    }
    
    
    
    public boolean darfMitarbeiterZugeordnetWerden(User mitarbeiter, List<Dienst> alleDienste, BigDecimal potenziellerVerdienst) {
        BigDecimal bisherigerVerdienst = BigDecimal.ZERO;

        for (Dienst d : alleDienste) {
            if (d.getMitarbeiter().getId().equals(mitarbeiter.getId())) {
                bisherigerVerdienst = bisherigerVerdienst.add(d.getVerdienst());
            }
        }

        return bisherigerVerdienst.add(potenziellerVerdienst).compareTo(MAX_MINIJOB_GRENZE) <= 0;
    }
    
}