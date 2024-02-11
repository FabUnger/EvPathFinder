package Data;

public class Node {

    private String id;
    private boolean hasChargingStation;

    public Node(String id, boolean hasChargingStation) {
        this.id = id;
        this.hasChargingStation = hasChargingStation;
    }

    public String getId() {
        return this.id;
    }

    public boolean getHasChargingStation()
    {
        return this.hasChargingStation;
    }

}
