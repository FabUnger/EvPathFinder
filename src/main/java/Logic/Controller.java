package Logic;

import Data.AlgorithmResult;

public interface Controller {

    void loadGraphFromDataBase();

    void selectAlgorithm(AlgorithmType type);

    AlgorithmResult executeAlgorithm();

}
