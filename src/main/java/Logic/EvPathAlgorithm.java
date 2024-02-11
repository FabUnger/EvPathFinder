package Logic;

import Data.AlgorithmResult;

public class EvPathAlgorithm implements PathAlgorithm {

    private AlgorithmType type;

    public EvPathAlgorithm() {
        type = AlgorithmType.EV;
    }

    @Override
    public AlgorithmType getType() {
        return this.type;
    }

    @Override
    public AlgorithmResult executeAlgorithm() {
        return null;
    }
}
