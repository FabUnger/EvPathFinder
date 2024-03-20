package Data;

public class Edge {

    private final String sourceId;
    private final String destinationId;

    private final double duration;
    private final double consumption;

    public Edge(String sourceId, String destinationId, double duration, double consumption){
        this.sourceId = sourceId;
        this.destinationId = destinationId;

        this.duration = duration;
        this.consumption = consumption;
    }

    public String getSourceId() {
        return this.sourceId;
    }

    public String getDestinationId() {
        return this.destinationId;
    }

    public double getDuration() {
        return this.duration;
    }

    public double getConsumption() {
        return this.consumption;
    }


}
