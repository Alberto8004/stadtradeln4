package de.SRH.stadtradeln.thread;

import de.SRH.stadtradeln.model.DateiManager;
import de.SRH.stadtradeln.model.StadtradelnModel;
import de.SRH.stadtradeln.view.StadtradelnView;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// Erbt von Thread und wird parallel zur Hauptanwendung durchgeführt
public class VerarbeitungThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(VerarbeitungThread.class.getName());
    private static final String DATEI_PFAD = "neuefahrten.csv";
    private final StadtradelnModel model;
    private final StadtradelnView view;
    private final DateiManager dateiManager;
    private boolean running = true; // Läuft bis explizit beendet

    // Initialisiert das Modell, die View und den DateiManager
    public VerarbeitungThread(StadtradelnModel model, DateiManager dateiManager, StadtradelnView view) {
        this.model = model;
        this.view = view;
        this.dateiManager = dateiManager;
    }

    // Ein separater Thread, der jede Minute prüft, ob "neuefahrten.csv" existiert und diese verarbeitet.
    @Override
    public void run() {
        while (running) {  // Läuft in einer Endlosschleife, solange running == true
            try {
                if (dateiManager.dateiExistiert(DATEI_PFAD)) {
                    verarbeiteNeueFahrten(); // Falls Datei existiert, wird diese Methode aufgerufen
                }
                Thread.sleep(60000); // 1 Minute warten bis zur nächten Überprüfung
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, "Thread wurde unterbrochen.", e);
                running = false;
            }
        }
    }

    // Methode zum sicheren Beenden des Threads
    public void stopThread() {
        running = false;
        this.interrupt();
    }

    // Verarbeitet die Datei "neuefahrten.csv"
    private void verarbeiteNeueFahrten() {
        List<String[]> neueFahrten = dateiManager.ladeNeueFahrten(DATEI_PFAD);
        List<String> fehlerhafteZeilen = new ArrayList<>();

        for (String[] teile : neueFahrten) {
            if (teile.length != 2) {
                fehlerhafteZeilen.add(String.join(",", teile));
                continue;
            }

            String nickname = teile[0].trim();
            int kilometer;

            try {
                kilometer = Integer.parseInt(teile[1].trim());
                if (kilometer < 0) {
                    throw new IllegalArgumentException("Kilometeranzahl darf nicht negativ sein.");
                }
                model.addFahrt(nickname, kilometer);
            } catch (IllegalArgumentException e) {
                fehlerhafteZeilen.add(String.join(",", teile));
                LOGGER.log(Level.WARNING, "Ungültige Datenzeile: " + String.join(",", teile));
            }
        }

        // Datei löschen, falls keine fehlerhaften Daten
        if (fehlerhafteZeilen.isEmpty()) {
            if (dateiManager.loescheDatei(DATEI_PFAD)) {
                view.addFeedbackMessage("Datei 'neuefahrten.csv' erfolgreich verarbeitet und gelöscht.");
            } else {
                view.addFeedbackMessage("Fehler beim Löschen der Datei.");
            }
        } else {
            view.addFeedbackMessage("Fehlerhafte Zeilen gefunden. Datei bleibt erhalten.");
        }

        // GUI aktualisieren und Daten speichern
        view.updateTable(model.getGruppenKilometer());
        model.speichereDaten();
    }
}
