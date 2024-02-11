package Persistence;

import Data.Edge;
import Data.Graph;
import Data.Node;

import java.util.List;

public interface GraphReader {

    Graph getGraph();

    Node getNodeById(String nodeId);

    List<Edge> getEdgesFromNode(String nodeId);

    List<Node> getAllNodes();
}
