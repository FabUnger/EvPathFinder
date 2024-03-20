package Data;

public class Node {

    private final String id;
    private final int chargingPower;

    public Node(String id, int chargingPower) {
        this.id = id;
        this.chargingPower = chargingPower;
    }

    public String getId() {
        return this.id;
    }

    public int getChargingPower()
    {
        return this.chargingPower;
    }

}
