package Data;

import java.util.ArrayList;
import java.util.List;

public class Path {

    private final List<VisitedNode> path;

    public Path(List<VisitedNode> path){
        this.path = new ArrayList<>(path);
    }

    public List<VisitedNode> getPath() {
        return this.path;
    }

    public double getTravelTimeOfNode(String id) {
        for (VisitedNode node : this.path) {
            if (node.getId().equals(id)) {
                return node.getTravelTime();
            }
        }
        return Double.MAX_VALUE;
    }

    public double getSocOfNode(String id) {
        for (VisitedNode node : this.path) {
            if (node.getId().equals(id)) {
                return node.getSoc();
            }
        }
        return -1;
    }

    public double getChargingTimeOfNode(String id) {
        for (VisitedNode node : this.path) {
            if (node.getId().equals(id)) {
                return node.getChargingTime();
            }
        }
        return 0.0;
    }

    public String getParentOfNode(String id) {
        for (VisitedNode node : this.path) {
            if (node.getId().equals(id)) {
                int index = this.path.indexOf(node);
                if (index - 1 < 0) return "";
                return this.path.get(index - 1).getId();
            }
        }
        return "";
    }

    public String getLastStation() {
        for (int i = this.path.size() - 1; i >= 0; i--) {
            VisitedNode node = this.path.get(i);
            if (node.getChargingTime() > 0.0) {
                return node.getId();
            }
        }
        return "";
    }
}
