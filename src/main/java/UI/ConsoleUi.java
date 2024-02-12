package UI;

import Data.AlgorithmResult;
import Logic.AlgorithmType;
import Logic.Controller;

import java.util.Scanner;

public class ConsoleUi {

    private final Controller controller;
    private final Scanner scanner;

    public ConsoleUi(Controller controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }

    public void startApp() {
        System.out.println("============================");
        System.out.println("Willkommen zum EV-Dijkstra");
        System.out.println("============================");

        this.menu();
    }

    private void menu() {
        boolean exit = false;

        while (!exit) {
            printMenu();
            System.out.print("Eingabe [1/2/3]: ");

            int option = this.scanner.nextInt();

            switch (option) {
                case 1:
                    selectAlgorithm();
                    break;
                case 2:
                    startAlgorithm();
                    break;
                case 3:
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
        System.out.println("1. Algorithmus auswählen");
        System.out.println("2. Algorithmus starten");
        System.out.println("3. Beenden");
    }

    private void selectAlgorithm() {
        boolean exit = false;

        while (!exit) {
            System.out.println("\nAlgorithmus waehlen:");
            System.out.println("1. EV-Dijkstra");
            System.out.println("2. Zurueck zum Menue");
            System.out.print("Eingabe [1/2]: ");
            int option = this.scanner.nextInt();

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
        AlgorithmResult result = this.controller.executeAlgorithm();

        if (result == null)
        {
            System.out.println("Algorithmus konnte nicht ausgefuehrt werden.");
            return;
        }

        System.out.println("Anzahl Schritte: " + result.getSteps());
        System.out.println("Dauer: " + result.getDuration());
    }

}