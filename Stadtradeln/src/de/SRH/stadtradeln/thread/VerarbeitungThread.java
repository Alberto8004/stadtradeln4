// VerarbeitungThread.java
package de.SRH.stadtradeln.thread;

import de.SRH.stadtradeln.model.DateiManager;
import de.SRH.stadtradeln.model.StadtradelnModel;
import de.SRH.stadtradeln.view.StadtradelnView;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VerarbeitungThread extends Thread {

    private static final Path NEUE_FAHRTEN_DATEI = Paths.get("C:/Users/startklar/IdeaProjects/Stadtradeln/neuefahrten.csv");
    private final StadtradelnModel model;
    private final DateiManager dateiManager;
    private final StadtradelnView view;

    public VerarbeitungThread(StadtradelnModel model, DateiManager dateiManager, StadtradelnView view) {
        this.model = model;
        this.dateiManager = dateiManager;
        this.view = view;
    }

    @Override
    public void run() {
        startSchedule();
    }

    private void startSchedule() {
        while (true) {
            try {
                Thread.sleep(60000); // Alle 60 Sekunden prüfen
                verarbeiteNeueFahrten();
            } catch (InterruptedException e) {
                System.out.println("Verarbeitungsthread unterbrochen: " + e.getMessage());
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void verarbeiteNeueFahrten() {
        System.out.println("Pfad zur Datei: " + NEUE_FAHRTEN_DATEI.toAbsolutePath());
        System.out.println("Existiert Datei? " + NEUE_FAHRTEN_DATEI.toFile().exists());

        File datei = NEUE_FAHRTEN_DATEI.toFile();
        if (!datei.exists()) {
            System.out.println("Keine neue Fahrten-Datei gefunden.");
            return;
        }


        try {
            List<String[]> neueFahrten = readAndProcessFile(NEUE_FAHRTEN_DATEI);
            if (!neueFahrten.isEmpty()) {
                neueFahrten.forEach(daten -> {
                    try {
                        String nickname = daten[0];
                        LocalDate datum = LocalDate.parse(daten[1]);
                        int kilometer = Integer.parseInt(daten[2]);

                        // Daten in das Model integrieren
                        model.addFahrt(nickname, kilometer);
                        dateiManager.speichereFahrt(nickname, kilometer, datum);
                        System.out.println("Fahrt hinzugefügt: " + nickname + ", " + kilometer + " km, " + datum);
                    } catch (Exception e) {
                        System.err.println("Fehler bei der Verarbeitung der Fahrt: " + String.join(",", daten));
                        e.printStackTrace();
                    }
                });

                view.updateTable(model.getGruppenKilometer()); // GUI aktualisieren
                deleteIfExists(NEUE_FAHRTEN_DATEI);
                System.out.println("Datei 'neuefahrten.csv' erfolgreich verarbeitet und gelöscht.");
            } else {
                System.out.println("Keine gültigen Daten zum Verarbeiten gefunden.");
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Verarbeiten der Datei 'neuefahrten.csv': " + e.getMessage());
        }
    }

    private List<String[]> readAndProcessFile(Path filePath) throws IOException {
        System.out.println("Datei-Inhalt wird gelesen...");
        List<String[]> neueFahrten = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            boolean isFirstLine = true; // Kopfzeile überspringen
            while ((line = reader.readLine()) != null) {
                System.out.println("Gelesene Zeile: " + line); // Debug-Ausgabe
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] daten = line.split(",");
                if (isValidFormat(daten)) {
                    neueFahrten.add(daten);
                } else {
                    System.out.println("Ungültige Zeile übersprungen: " + line);
                }
            }
        }
        return neueFahrten;
    }


    private boolean isValidFormat(String[] daten) {
        System.out.println("Zeile wird validiert: " + Arrays.toString(daten)); // Debug-Ausgabe
        return daten.length == 3 &&
                !daten[0].trim().isEmpty() && // Nickname darf nicht leer sein
                daten[1].trim().matches("\\d{4}-\\d{2}-\\d{2}") && // Datum im Format YYYY-MM-DD
                daten[2].trim().matches("\\d+"); // Kilometer muss eine positive Zahl sein
    }


    private void deleteIfExists(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Fehler beim Löschen der Datei " + filePath + ": " + e.getMessage());
        }
    }
}
