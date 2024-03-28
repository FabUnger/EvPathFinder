package Manager;

import Logic.*;
import Persistence.Neo4jReader;
import UI.ConsoleUi;

import java.util.ArrayList;
import java.util.List;

public class Factory {

    public static void main(String[] args) {
        Neo4jReader reader = new Neo4jReader(Properties.URI, Properties.USERNAME, Properties.PASSWORD);

        List<PathAlgorithm> algorithmList = new ArrayList<>();
        EvPathAlgorithm evPathAlgorithm = new EvPathAlgorithm(reader);
        algorithmList.add(evPathAlgorithm);

        Context context = new Context(algorithmList);
        context.setPathAlgorithm(AlgorithmType.EV);

        BusinessController controller = new BusinessController(context, reader);

        ConsoleUi consoleUi = new ConsoleUi(controller);
        consoleUi.startApp();
    }

}
