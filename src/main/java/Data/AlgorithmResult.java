package Data;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AlgorithmResult {

    private int steps;
    private double duration;
    private Map<Node, Double> path;
    private double travelTime;

    public AlgorithmResult(int steps, double duration, Map<Node, Double> path, double travelTime) {
        this.steps = steps;
        this.duration = duration;
        this.path = new LinkedHashMap<>(path);
        this.travelTime = travelTime;
    }

    public int getSteps() {
        return this.steps;
    }

    public double getDuration() {
        return this.duration;
    }

    public Map<Node, Double> getPath() {
        return this.path;
    }

    public double getTravelTime() {
        return this.travelTime;
    }
}
