package com.maamouniverse.theaterapp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DienstPlanerService {

    private final AbrechnungsService abrechnungsService;

    public DienstPlanerService(AbrechnungsService abrechnungsService) {
        this.abrechnungsService = abrechnungsService;
    }

    public List<Dienst> generiereMonatsplan(
            List<Vorstellung> vorstellungen, 
            List<User> mitarbeiter, 
            List<Verfuegbarkeit> verfuegbarkeiten) {

        List<Dienst> generierteDienste = new ArrayList<>();
        long dienstIdCounter = 1;

        // 1. Vorstellungen chronologisch sortieren
        List<Vorstellung> sortierteVorstellungen = vorstellungen.stream()
                .filter(v -> !v.isIstAbgesagt())
                .sorted(Comparator.comparing(Vorstellung::getBeginnVorstellung))
                .collect(Collectors.toList());

        for (Vorstellung v : sortierteVorstellungen) {
            LocalDate terminDatum = v.getBeginnVorstellung().toLocalDate();

            // 2. Wer kann an diesem Tag arbeiten?
            List<User> verfuegbareLeute = findeVerfuegbareMitarbeiter(terminDatum, mitarbeiter, verfuegbarkeiten);

            // 3. Standort-Bedarf ermitteln
            int benoetigteLangeDienste = 0;
            int benoetigteKurzeDienste = 0;

            switch (v.getStandort()) {
                case GH:
                    benoetigteLangeDienste = 8;
                    benoetigteKurzeDienste = 12;
                    break;
                case AFW:
                    // Beispielwerte für andere Stätten - hier anpassen!
                    benoetigteLangeDienste = 2;
                    benoetigteKurzeDienste = 4;
                    break;
                default:
                    benoetigteLangeDienste = 1;
                    benoetigteKurzeDienste = 2;
                    break;
            }

            // -------------------------------------------------------------
            // A. GENAU EIN OBERSCHLIESSER (Immer LANG/PREMIERE)
            // -------------------------------------------------------------
            User gewaehlterOberschliesser = findeMitarbeiterMitWenigstemVerdienst(
                    verfuegbareLeute, UserRole.OBERSCHLIESSER, generierteDienste, v, DienstTyp.LANG);

            if (gewaehlterOberschliesser != null) {
                LocalDateTime start = abrechnungsService.berechneDienstbeginn(v, UserRole.OBERSCHLIESSER);
                LocalDateTime ende = start.plusHours(4); // Dummy-Planungsende
                
                Dienst d = new Dienst(dienstIdCounter++, gewaehlterOberschliesser, v, DienstTyp.LANG, start, ende);
                d.setVerdienst(abrechnungsService.berechneVerdienst(d));
                generierteDienste.add(d);
            } else {
                System.out.println("[WARNUNG] Kein Oberschließer gefunden für " + v.getTitel() + " in " + v.getStandort());
            }

            // -------------------------------------------------------------
            // B. NORMALE SCHLIESSER: LANGE DIENSTE BESETZEN
            // -------------------------------------------------------------
            for (int i = 0; i < benoetigteLangeDienste; i++) {
                User gewaehlterSchliesser = findeNaechstenSchliesser(verfuegbareLeute, generierteDienste, v, DienstTyp.LANG);
                if (gewaehlterSchliesser != null) {
                    LocalDateTime start = abrechnungsService.berechneDienstbeginn(v, UserRole.SCHLIESSER);
                    LocalDateTime ende = start.plusHours(4); 
                    
                    Dienst d = new Dienst(dienstIdCounter++, gewaehlterSchliesser, v, DienstTyp.LANG, start, ende);
                    d.setVerdienst(abrechnungsService.berechneVerdienst(d));
                    generierteDienste.add(d);
                }
            }

            // -------------------------------------------------------------
            // C. NORMALE SCHLIESSER: KURZE DIENSTE BESETZEN
            // -------------------------------------------------------------
            for (int i = 0; i < benoetigteKurzeDienste; i++) {
                User gewaehlterSchliesser = findeNaechstenSchliesser(verfuegbareLeute, generierteDienste, v, DienstTyp.KURZ);
                if (gewaehlterSchliesser != null) {
                    LocalDateTime start = abrechnungsService.berechneDienstbeginn(v, UserRole.SCHLIESSER);
                    LocalDateTime ende = start.plusMinutes(90); // Kurzer Dienst ist starr 1,5 Std.
                    
                    Dienst d = new Dienst(dienstIdCounter++, gewaehlterSchliesser, v, DienstTyp.KURZ, start, ende);
                    d.setVerdienst(abrechnungsService.berechneVerdienst(d));
                    generierteDienste.add(d);
                }
            }
        }

        return generierteDienste;
    }

    // --- HILFSFUNKTIONEN ---

    private User findeNaechstenSchliesser(List<User> verfuegbareLeute, List<Dienst> generierteDienste, Vorstellung v, DienstTyp typ) {
        List<User> freieSchliesser = verfuegbareLeute.stream()
                .filter(u -> u.getRole() == UserRole.SCHLIESSER)
                .filter(u -> !istBereitsEingeteilt(u, v, generierteDienste))
                .collect(Collectors.toList());

        return findeMitarbeiterMitWenigstemVerdienst(freieSchliesser, UserRole.SCHLIESSER, generierteDienste, v, typ);
    }

    private List<User> findeVerfuegbareMitarbeiter(LocalDate datum, List<User> mitarbeiter, List<Verfuegbarkeit> verfuegbarkeiten) {
        List<User> verfuegbar = new ArrayList<>();
        for (Verfuegbarkeit vf : verfuegbarkeiten) {
            if (vf.getDatum().equals(datum) && vf.isKannArbeiten()) {
                verfuegbar.add(vf.getMitarbeiter());
            }
        }
        return verfuegbar;
    }

    private User findeMitarbeiterMitWenigstemVerdienst(List<User> pool, UserRole rolle, List<Dienst> aktuelleDienste, Vorstellung v, DienstTyp typ) {
        User bestCandidate = null;
        BigDecimal lowestVerdienst = BigDecimal.valueOf(Double.MAX_VALUE);

        // Fiktiven Dienst simulieren für die Budget-Prüfung
        LocalDateTime start = v.getBeginnVorstellung();
        LocalDateTime ende = typ == DienstTyp.KURZ ? start.plusMinutes(90) : start.plusHours(4);
        Dienst testDienst = new Dienst(0L, pool.isEmpty() ? null : pool.get(0), v, typ, start, ende);
        BigDecimal fiktiverLohn = abrechnungsService.berechneVerdienst(testDienst);

        for (User u : pool) {
            if (u.getRole() != rolle) continue;

            if (!abrechnungsService.darfMitarbeiterZugeordnetWerden(u, aktuelleDienste, fiktiverLohn)) {
                continue; // Mitarbeiter überspringen, falls > 556€
            }

            BigDecimal aktuellerVerdienst = berechneBisherigenMonatsVerdienst(u, aktuelleDienste);
            if (aktuellerVerdienst.compareTo(lowestVerdienst) < 0) {
                lowestVerdienst = aktuellerVerdienst;
                bestCandidate = u;
            }
        }
        return bestCandidate;
    }
    //Berechnen von Verdienen im Monat
    private BigDecimal berechneBisherigenMonatsVerdienst(User u, List<Dienst> dienste) {
        BigDecimal summe = BigDecimal.ZERO;
        for (Dienst d : dienste) {
            if (d.getMitarbeiter().getId().equals(u.getId())) {
                summe = summe.add(d.getVerdienst());
            }
        }
        return summe;
    }

    private boolean istBereitsEingeteilt(User u, Vorstellung v, List<Dienst> dienste) {
        return dienste.stream().anyMatch(d -> d.getMitarbeiter().getId().equals(u.getId()) && d.getVorstellung().getId().equals(v.getId()));
    }
}