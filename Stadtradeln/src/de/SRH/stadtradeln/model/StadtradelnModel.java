package de.SRH.stadtradeln.model;

import java.util.*;

public class StadtradelnModel {
    private Map<String, List<String>> gruppenMitglieder;
    private Map<String, Integer> gruppenKilometer;
    private Map<String, Integer> fahrerKilometer;
    private Map<String, String[]> gruppenVerantwortliche;
    private final DateiManager dateiManager;

    public StadtradelnModel(DateiManager dateiManager) {
        this.dateiManager = new DateiManager();
        Map<String, Object> daten = this.dateiManager.ladeDaten();
        gruppenMitglieder = (Map<String, List<String>>) daten.getOrDefault("gruppenMitglieder", new HashMap<>());
        gruppenKilometer = (Map<String, Integer>) daten.getOrDefault("gruppenKilometer", new HashMap<>());
        fahrerKilometer = (Map<String, Integer>) daten.getOrDefault("fahrerKilometer", new HashMap<>());
        gruppenVerantwortliche = (Map<String, String[]>) daten.getOrDefault("gruppenVerantwortliche", new HashMap<>());
    }

    public void addGruppe(String gruppe, String verantwortlicherName, String email) {
        if (gruppenMitglieder.containsKey(gruppe)) {
            throw new IllegalArgumentException("Gruppe mit dem Namen '" + gruppe + "' existiert bereits.");
        }
        gruppenMitglieder.put(gruppe, new ArrayList<>());
        gruppenKilometer.put(gruppe, 0);
        gruppenVerantwortliche.put(gruppe, new String[]{verantwortlicherName, email});
        addFahrer(gruppe, verantwortlicherName);
    }

    public void addFahrer(String gruppe, String nickname) {
        if (fahrerKilometer.containsKey(nickname)) {
            throw new IllegalArgumentException("Fahrer mit dem Nickname '" + nickname + "' existiert bereits.");
        }
        if (gruppenMitglieder.containsKey(gruppe)) {
            gruppenMitglieder.get(gruppe).add(nickname);
            fahrerKilometer.putIfAbsent(nickname, 0);
        }
    }

    public void addFahrt(String nickname, int kilometer) {
        System.out.println("Fahrt wird hinzugefügt: " + nickname + ", Kilometer: " + kilometer); // Debug-Ausgabe
        if (!gruppenMitglieder.values().stream().anyMatch(liste -> liste.contains(nickname))) {
            throw new IllegalArgumentException("Fahrer nicht in einer Gruppe registriert.");
        }

        fahrerKilometer.put(nickname, fahrerKilometer.getOrDefault(nickname, 0) + kilometer);

        gruppenKilometer.forEach((gruppe, km) -> {
            if (gruppenMitglieder.get(gruppe).contains(nickname)) {
                gruppenKilometer.put(gruppe, km + kilometer);
            }
        });

        speichereDaten();
    }


    public void speichereDaten() {
        Map<String, Object> daten = new HashMap<>();
        daten.put("gruppenMitglieder", gruppenMitglieder);
        daten.put("gruppenKilometer", gruppenKilometer);
        daten.put("fahrerKilometer", fahrerKilometer);
        daten.put("gruppenVerantwortliche", gruppenVerantwortliche);
        dateiManager.speichereDaten(daten);
    }

    public Map<String, Integer> getGruppenKilometer() {
        return gruppenKilometer;
    }

    public Map<String, List<String>> getGruppenMitglieder() {
        return gruppenMitglieder;
    }

    public List<String[]> ladeFahrten() {
        return dateiManager.ladeFahrten();
    }

    // Debugging-Methode zur Ausgabe der aktuellen Gruppen-Mitglieder
    public void debugGruppenMitglieder() {
        System.out.println("Aktuelle Gruppenmitglieder:");
        for (Map.Entry<String, List<String>> entry : gruppenMitglieder.entrySet()) {
            System.out.println("Gruppe: " + entry.getKey() + " -> Mitglieder: " + entry.getValue());
        }
    }
}
