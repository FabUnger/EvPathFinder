package Logic;

import Data.AlgorithmResult;
import Data.Node;
import Persistence.GraphReader;

public abstract class PathAlgorithm {

    protected final GraphReader reader;

    public PathAlgorithm(GraphReader reader)
    {
        this.reader = reader;
    }

    public abstract AlgorithmType getType();

    public AlgorithmResult startAlgorithm(Node start, Node end, double maxSoc, double initialCharge, int minChargingTime) {
        long startTime = System.currentTimeMillis();

        AlgorithmResult result = this.executeAlgorithm(start, end, maxSoc, initialCharge, minChargingTime);

        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;

        return new AlgorithmResult(result.getSteps(), duration, result.getPath(), result.getTravelTime());
    }

    protected abstract AlgorithmResult executeAlgorithm(Node start, Node end, double maxSoc, double initialCharge, int minChargingTime);

    protected double calculateNewSoc(double maxSoc, double soc, double chargingTime, double chargingPower) {
        double chargedEnergy = (chargingTime / 60.0) * chargingPower;
        double newSoc = soc + chargedEnergy;
        return Math.min(newSoc, maxSoc);
    }

    protected double calculateAdditionalChargeTime(double currentSoc, double necessarySoc, double chargingPower) {
        if (chargingPower == 0.0 || necessarySoc < currentSoc)
            return 0.0;
        double necessaryChargedEnergy = necessarySoc - currentSoc;
        return (necessaryChargedEnergy / chargingPower) * 60.0;
    }
}
