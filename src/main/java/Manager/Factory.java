package Manager;

import Logic.BusinessController;
import Logic.Context;
import Logic.EvPathAlgorithm;
import Logic.PathAlgorithm;
import Persistence.Neo4jReader;
import UI.ConsoleUi;

import java.util.ArrayList;
import java.util.List;

public class Factory {

    public static void main(String[] args) {
        Neo4jReader reader = new Neo4jReader(Properties.URI, Properties.USERNAME, Properties.PASSWORD);

        List<PathAlgorithm> algorithmList = new ArrayList<>();
        algorithmList.add(new EvPathAlgorithm());

        Context context = new Context(algorithmList);

        BusinessController controller = new BusinessController(context, reader);

        ConsoleUi consoleUi = new ConsoleUi(controller);
        consoleUi.startApp();
    }

}
