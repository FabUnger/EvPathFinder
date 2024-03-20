package Logic;

import Data.AlgorithmResult;
import Data.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context {

    private PathAlgorithm selectedPathAlgorithm;
    private Map<AlgorithmType, PathAlgorithm> pathAlgorithmMap;

    public Context(List<PathAlgorithm> algorithms) {
        this.pathAlgorithmMap = new HashMap<>();
        for (PathAlgorithm alg : algorithms) {
            this.pathAlgorithmMap.put(alg.getType(), alg);
        }
    }

    public void setPathAlgorithm(AlgorithmType type) {
        switch (type) {
            case EV -> {
                if (this.pathAlgorithmMap.get(AlgorithmType.EV) != null)
                    this.selectedPathAlgorithm = pathAlgorithmMap.get(AlgorithmType.EV);
            }
        }
    }

    public AlgorithmResult executeAlgorithm(Node start, Node end, double maxSoc, double initialCharge, int minChargingTime) {
        return this.selectedPathAlgorithm.startAlgorithm(start, end, maxSoc, initialCharge, minChargingTime);
    }

}
