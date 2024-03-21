package Data;


public class AlgorithmResult {

    private int steps;
    private double duration;
    private Path path;
    private double travelTime;

    public AlgorithmResult(int steps, double duration, Path path, double travelTime) {
        this.steps = steps;
        this.duration = duration;
        this.path = path;
        this.travelTime = travelTime;
    }

    public int getSteps() {
        return this.steps;
    }

    public double getDuration() {
        return this.duration;
    }

    public Path getPath() {
        return this.path;
    }

    public double getTravelTime() {
        return this.travelTime;
    }
}
