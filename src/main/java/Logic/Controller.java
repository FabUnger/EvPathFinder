package Logic;

import Data.AlgorithmResult;
import Data.Graph;

public interface Controller {

    Graph getGraphFromDatabase();

    void selectAlgorithm(AlgorithmType type);

    AlgorithmResult executeAlgorithm();

}
