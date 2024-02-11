package Data;

public class Edge {

    private Node source;
    private Node destination;

    private double duration;
    private double consumption;

    public Edge(Node source, Node destination, double duration, double consumption){
        this.source = source;
        this.destination = destination;

        this.duration = duration;
        this.consumption = consumption;
    }

    public Node getSource() {
        return this.source;
    }

    public Node getDestination() {
        return this.destination;
    }

    public double getDuration() {
        return this.duration;
    }

    public double getConsumption() {
        return this.consumption;
    }


}
