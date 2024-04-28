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

    public double getTravelTimeOfNode(VisitedNodeId id) {
        for (VisitedNode node : this.path) {
            if (node.getId().equals(id)) {
                return node.getTravelTime();
            }
        }
        return Double.MAX_VALUE;
    }

    public double getSocOfNode(VisitedNodeId id) {
        for (VisitedNode node : this.path) {
            if (node.getId().equals(id)) {
                return node.getSoc();
            }
        }
        return -1;
    }

    public double getChargingTimeOfNode(VisitedNodeId id) {
        for (VisitedNode node : this.path) {
            if (node.getId().equals(id)) {
                return node.getChargingTime();
            }
        }
        return 0.0;
    }

    public VisitedNodeId getParentOfNode(VisitedNodeId id) {
        for (VisitedNode node : this.path) {
            if (node.getId().equals(id)) {
                int index = this.path.indexOf(node);
                if (index - 1 < 0) return null;
                return this.path.get(index - 1).getId();
            }
        }
        return null;
    }

    public VisitedNodeId getLastStation() {
        for (int i = this.path.size() - 1; i >= 0; i--) {
            VisitedNode node = this.path.get(i);
            if (node.getChargingTime() > 0.0) {
                return node.getId();
            }
        }
        return null;
    }

    public VisitedNode getLastNode() {
        int index = path.size() - 1;
        return this.path.get(index);
    }

    public VisitedNode getNodeById(VisitedNodeId id) {
        for (VisitedNode node : this.path) {
            if (node.getId().equals(id)) {
                return node;
            }
        }
        return null;
    }
}
