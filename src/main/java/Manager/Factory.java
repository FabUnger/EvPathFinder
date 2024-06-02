package Manager;

import Logic.*;
import Persistence.Neo4jReader;
import UI.ConsoleUi;

import java.util.ArrayList;
import java.util.List;

public class Factory {

    public static void main(String[] args) {
        Neo4jReader reader = new Neo4jReader();

        List<PathAlgorithm> algorithmList = new ArrayList<>();
        EvPathAlgorithm evPathAlgorithm = new EvPathAlgorithm(reader);
        algorithmList.add(evPathAlgorithm);

        PathAlgorithmContext context = new PathAlgorithmContext(algorithmList);
        context.setPathAlgorithm(AlgorithmType.DIBASEM);

        BusinessController controller = new BusinessController(context, reader);

        ConsoleUi consoleUi = new ConsoleUi(controller);
        consoleUi.startApp();
    }

}
