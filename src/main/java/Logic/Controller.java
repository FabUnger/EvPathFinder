package Logic;

import Data.AlgorithmResult;
import Persistence.Properties;

import java.util.List;

public interface Controller {

    void initializeDatabase(Properties properties);

    List<String> getAllNodeIds();

    boolean isNodeExisting(String id);

    void selectAlgorithm(AlgorithmType type);

    AlgorithmResult executeAlgorithm(String startId, String endId, double maxSoc, double initialCharge, int minChargingTime);

    String getSelectedAlgorithmTypeAsString();

}
