package de.SRH.stadtradeln.main;

import de.SRH.stadtradeln.controller.StadtradelnController;
import de.SRH.stadtradeln.model.DateiManager;
import de.SRH.stadtradeln.model.StadtradelnModel;
import de.SRH.stadtradeln.thread.VerarbeitungThread;
import de.SRH.stadtradeln.view.StadtradelnView;

public class Main {
    public static void main(String[] args) {
        // Initialisierung der benötigten Objekte für Klassenübergreifende Interaktion
        DateiManager dateiManager = new DateiManager();
        StadtradelnModel model = new StadtradelnModel(dateiManager);
        StadtradelnView view = new StadtradelnView();
        StadtradelnController controller = new StadtradelnController(model, view);

        // Verbindung zwischen View und Controller
        view.setController(controller);

        // Startet den Verarbeitungsthread für neue Fahrten
        VerarbeitungThread verarbeitungThread = new VerarbeitungThread(model, dateiManager, view);
        verarbeitungThread.start();
        System.out.println("VerarbeitungThread wurde gestartet.");

        // Sicherstellen, dass Daten beim Beenden gespeichert werden
        Runtime.getRuntime().addShutdownHook(new Thread(model::speichereDaten));
    }
}
