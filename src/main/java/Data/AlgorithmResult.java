package Data;

import java.util.ArrayList;
import java.util.List;

public class AlgorithmResult {

    private int steps;
    private double duration;
    private List<Node> path;

    public AlgorithmResult(int steps, double duration, List<Node> path) {
        this.steps = steps;
        this.duration = duration;
        this.path = new ArrayList<>(path);
    }

    public int getSteps() {
        return this.steps;
    }

    public double getDuration() {
        return this.duration;
    }

    public List<Node> getPath() {
        return this.path;
    }
}
