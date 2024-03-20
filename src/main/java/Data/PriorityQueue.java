package Data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PriorityQueue {
    private final Map<String, Double> nodeMap = new HashMap<>();

    public void put(String nodeId, double priority) {
        this.nodeMap.put(nodeId, priority);
    }

    public String poll() {
        Map.Entry<String, Double> highestPriorityEntry = Collections.min(this.nodeMap.entrySet(), Map.Entry.comparingByValue());
        this.nodeMap.remove(highestPriorityEntry.getKey());
        return highestPriorityEntry.getKey();
    }

    public boolean containsNodeId(String nodeId) {
        for (String id : nodeMap.keySet()) {
            if (id.equals(nodeId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return this.nodeMap.isEmpty();
    }

}
