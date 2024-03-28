package UI;

import Data.AlgorithmResult;
import Data.Node;
import Data.VisitedNode;
import Logic.AlgorithmType;
import Logic.Controller;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ConsoleUi {

    private final Controller controller;
    private final Scanner scanner;

    public ConsoleUi(Controller controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }

    public void startApp() {
        System.out.println("==========");
        System.out.println("Willkommen");
        System.out.println("==========");

        this.menu();

        this.scanner.close();
    }

    private void menu() {
        boolean exit = false;

        while (!exit) {
            printMenu();
            System.out.print("Eingabe [1/2/3/4]: ");

            int option = this.scanner.nextInt();
            scanner.nextLine();
            switch (option) {
                case 1:
                    selectAlgorithm();
                    break;
                case 2:
                    startAlgorithm();
                    break;
                case 3:
                    this.printAllNodeIds();
                    break;
                case 4:
                    exit = true;
                    System.out.println("Anwendung wird beendet...");
                    break;
                default:
                    System.out.println("Ungültige Auswahl. Bitte versuchen Sie es erneut.");
                    break;
            }
        }
    }

    private void printMenu() {
        System.out.println("\nHauptmenü:");
        System.out.println("1. Algorithmus auswählen [Aktuell: " + this.controller.getSelectedAlgorithmTypeAsString() + "]");
        System.out.println("2. Algorithmus starten");
        System.out.println("3. IDs aller Nodes aus Datenbank ausgeben");
        System.out.println("4. Beenden");
    }

    private void selectAlgorithm() {
        boolean exit = false;

        while (!exit) {
            System.out.println("\nAlgorithmus waehlen:");
            System.out.println("1. EV-Dijkstra");
            System.out.println("2. Zurueck zum Menue");
            System.out.print("Eingabe [1/2]: ");
            int option = this.scanner.nextInt();
            scanner.nextLine();
            switch (option) {
                case 1:
                    this.controller.selectAlgorithm(AlgorithmType.EV);
                    break;
                case 2:
                    exit = true;
                    break;
                default:
                    System.out.println("Ungültige Auswahl. Bitte versuchen Sie es erneut.");
                    break;
            }
        }
    }

    private void startAlgorithm() {

        System.out.print("ID des Startknotens eingeben: ");
        String startId = scanner.nextLine();
        if (!this.controller.isNodeExisting(startId)) {
            System.out.println("Knoten nicht in DB enthalten. Breche Start ab.");
            return;
        }

        System.out.print("ID des Zielknotens eingeben: ");
        String endId = scanner.nextLine();
        if (!this.controller.isNodeExisting(endId)) {
            System.out.println("Knoten nicht in DB enthalten. Breche Start ab.");
            return;
        }

        System.out.print("Akkukapazitaet in kWh eingeben: ");
        double maxSoc = scanner.nextDouble();
        scanner.nextLine();
        if (maxSoc <= 0.0) {
            System.out.println("Bitte einen Wert groesser 0.0 eingeben. Breche Start ab.");
            return;
        }

        System.out.print("Initiale Akkukapazitaet in kWh eingeben: ");
        double initialCharge = scanner.nextDouble();
        scanner.nextLine();
        if (initialCharge <= 0.0 || initialCharge > maxSoc) {
            System.out.println("Bitte einen Wert groesser 0.0 bzw. kleiner als maximale Akkukapazitaet eingeben. Breche Start ab.");
            return;
        }

        System.out.print("Gewuenschte minimale Ladezeit eingeben: ");
        int minChargingTime = scanner.nextInt();
        scanner.nextLine();
        if (minChargingTime < 0.0) {
            System.out.println("Bitte einen Wert groesser 0.0 eingeben. Breche Start ab.");
            return;
        }

        AlgorithmResult result = this.controller.executeAlgorithm(startId, endId, maxSoc, initialCharge, minChargingTime);

        if (result == null)
        {
            System.out.println("Algorithmus konnte nicht ausgefuehrt werden.");
            return;
        }

        System.out.println("=======");
        System.out.println("Analyse");
        System.out.println("=======");
        System.out.println("Anzahl Schritte: " + result.getSteps());
        System.out.println("Dauer: " + result.getDuration() + "ms");

        System.out.println("========");
        System.out.println("Ergebnis");
        System.out.println("========");

        List<VisitedNode> path = result.getPath().getPath();

        if (!path.isEmpty()) {
            System.out.println("Route:");
            int nodeCount = 1;
            for (VisitedNode node : result.getPath().getPath()) {
                if (node.getChargingTime() > 0.0) {
                    System.out.println(nodeCount + ".: " + node.getId() + " | SoC: " + node.getSoc() + " | Ladezeit: " + node.getChargingTime());
                } else {
                    System.out.println(nodeCount + ".: " + node.getId() + " | SoC: " + node.getSoc());
                }
                nodeCount++;
            }

            System.out.println("Gesamtreisezeit: " + result.getTravelTime());
        } else {
            System.out.println("Es konnte keine Route von dem Startknoten " + startId + " zu dem Zielknoten " + endId + " gefunden werden. " +
                    "Moeglicherweise reichte die Akkukapazitaet hierfuer nicht aus.");
        }


    }

    private void printAllNodeIds() {
        System.out.println("Alle IDs in Datenbank:");
        System.out.println();
        List<String> ids = this.controller.getAllNodeIds();
        for (String id : ids) {
            System.out.print(id + ", ");
        }

    }

}