package Logic;

import Data.AlgorithmResult;

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
            case DIJKSTRA -> {
                if (this.pathAlgorithmMap.get(AlgorithmType.DIJKSTRA) != null)
                    this.selectedPathAlgorithm = pathAlgorithmMap.get(AlgorithmType.DIJKSTRA);
            }
            case EV -> {
                if (this.pathAlgorithmMap.get(AlgorithmType.EV) != null)
                    this.selectedPathAlgorithm = pathAlgorithmMap.get(AlgorithmType.EV);
            }
        }
    }

    public AlgorithmResult executeAlgorithm() {
        return this.selectedPathAlgorithm.executeAlgorithm();
    }

}
