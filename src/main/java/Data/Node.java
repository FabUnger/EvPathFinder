package Data;

public class Node {

    String id;
    boolean hasChargingStation;

    Node(String id, boolean hasChargingStation) {
        this.id = id;
        this.hasChargingStation = hasChargingStation;
    }

    public String GetId() {
        return this.id;
    }

    public boolean GetHasChargingStation()
    {
        return this.hasChargingStation;
    }

}
