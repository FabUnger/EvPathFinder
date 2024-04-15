package Logic;

import Data.AlgorithmResult;
import Data.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Logic.AlgorithmType.EV;

public class PathAlgorithmContext {

    private PathAlgorithm selectedPathAlgorithm;
    private Map<AlgorithmType, PathAlgorithm> pathAlgorithmMap;

    public PathAlgorithmContext(List<PathAlgorithm> algorithms) {
        this.pathAlgorithmMap = new HashMap<>();
        for (PathAlgorithm alg : algorithms) {
            this.pathAlgorithmMap.put(alg.getType(), alg);
        }
    }

    public void setPathAlgorithm(AlgorithmType type) {
        switch (type) {
            case EV -> {
                if (this.pathAlgorithmMap.get(EV) != null)
                    this.selectedPathAlgorithm = pathAlgorithmMap.get(EV);
            }
        }
    }

    public String getSelectedTypeAsString() {
        switch(this.selectedPathAlgorithm.getType()) {
            case EV -> {
                return "EV-Dijkstra";
            }
            default -> {
                return "Kein Algorithmus ausgewählt.";
            }
        }
    }

    public AlgorithmResult executeAlgorithm(Node start, Node end, double maxSoc, double initialCharge, int minChargingTime) {
        return this.selectedPathAlgorithm.startAlgorithm(start, end, maxSoc, initialCharge, minChargingTime);
    }

}
