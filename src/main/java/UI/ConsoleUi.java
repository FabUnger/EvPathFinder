package UI;

import Logic.Controller;

public class ConsoleUi {

    private final Controller controller;

    public ConsoleUi(Controller controller) {
        this.controller = controller;
    }

    public void startApp() {
        System.out.println("App initialisiert und gestartet!");
    }

}