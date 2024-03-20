package Persistence;

import Data.Edge;
import Data.Node;

import java.util.List;

public interface GraphReader {

    Node getNodeById(String nodeId);

    List<Edge> getEdgesFromSourceNode(String nodeId);

    Edge getShortestEdgeBetweenNodes(String sourceId, String destinationId);

    List<String> getAllNodeIds();

    boolean isNodeExisting(String id);
}
