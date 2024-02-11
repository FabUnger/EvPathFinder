package Manager;

import Logic.BusinessController;
import Logic.Context;
import Logic.EvPathAlgorithm;
import Logic.PathAlgorithm;
import UI.ConsoleUi;

import java.util.ArrayList;
import java.util.List;

public class Factory {

    public static void main(String[] args) {
        List<PathAlgorithm> algorithmList = new ArrayList<>();
        algorithmList.add(new EvPathAlgorithm());

        Context context = new Context(algorithmList);

        BusinessController controller = new BusinessController(context);

        ConsoleUi consoleUi = new ConsoleUi(controller);
        consoleUi.startApp();
    }

}
