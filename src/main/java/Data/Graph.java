package Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {

    Map<String, Node> nodes = new HashMap<>();
    List<Edge> edges = new ArrayList<>();

    public void addNode(Node node) {
        this.nodes.put(node.id, node);
    }

    public void addEdge(Edge edge) {
        this.edges.add(edge);
    }

    public Node getNode(String id) {
        return this.nodes.get(id);
    }

}
