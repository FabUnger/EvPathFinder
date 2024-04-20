package Data;

public class VisitedNode {
    private final VisitedNodeId id;
    private final double travelTime;
    private final double soc;
    private final double chargingTime;

    public VisitedNode(String id, double travelTime, double soc, double chargingTime) {
        this.id = new VisitedNodeId(id);
        this.travelTime = travelTime;
        this.soc = soc;
        this.chargingTime = chargingTime;
    }

    public VisitedNodeId getId() {
        return this.id;
    }

    public double getTravelTime() {
        return this.travelTime;
    }

    public double getSoc() {
        return this.soc;
    }

    public double getChargingTime()
    {
        return this.chargingTime;
    }

}
