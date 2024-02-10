package Data;

public class Edge {

    Node source;
    Node destination;

    double duration;
    double consumption;

    public Edge(Node source, Node destination, double duration, double consumption){
        this.source = source;
        this.destination = destination;

        this.duration = duration;
        this.consumption = consumption;
    }

    public Node GetSource() {
        return this.source;
    }

    public Node GetDestination() {
        return this.destination;
    }

    public double GetDuration() {
        return this.duration;
    }

    public double GetConsumption() {
        return this.consumption;
    }


}
