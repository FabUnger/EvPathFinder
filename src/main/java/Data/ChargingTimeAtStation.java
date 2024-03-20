package Data;

public class ChargingTimeAtStation {

    private final String id;
    private final double chargingTime;

    public ChargingTimeAtStation(String id, double chargingTime) {
        this.id = id;
        this.chargingTime = chargingTime;
    }

    public String getId() {
        return this.id;
    }

    public double getChargingTime() {
        return this.chargingTime;
    }

}
