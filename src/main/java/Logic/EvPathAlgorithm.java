package Logic;

import Data.*;
import Data.PriorityQueue;
import Persistence.GraphReader;

import java.util.*;

public class EvPathAlgorithm extends PathAlgorithm {

    private final AlgorithmType type;

    private Map<String, Path> pathOfNode;

    public EvPathAlgorithm(GraphReader reader) {
        super(reader);
        type = AlgorithmType.EV;
    }

    @Override
    public AlgorithmType getType() {
        return this.type;
    }

    @Override
    protected AlgorithmResult executeAlgorithm(Node start, Node end, double maxSoc, double initialCharge, int minChargingTime) {
        this.initialize(start, initialCharge);

        PriorityQueue queue = new PriorityQueue();
        for (String id : this.reader.getAllNodeIds()) {
            queue.put(id, pathOfNode.get(id).getTravelTimeOfNode(id));
        }

        int steps = 0;

        while (!queue.isEmpty()) {
            steps++;

            Node u = this.reader.getNodeById(queue.poll());

            if (u.getId().equals(end.getId())) {
                // Endknoten gefunden. Dessen Nachbarn muessen nicht mehr ueberprueft werden.
                break;
            }

            List<Edge> edgesFromU = this.reader.getEdgesFromSourceNode(u.getId());
            for (Edge edgeFromU : edgesFromU) {
                Node v = this.reader.getNodeById(edgeFromU.getDestinationId());
                if (!queue.containsNodeId(v.getId())) continue;

                double duration = edgeFromU.getDuration();
                double consumption = edgeFromU.getConsumption();

                Path pathOfU = this.pathOfNode.get(u.getId());

                double currentTravelTime = pathOfU.getTravelTimeOfNode(u.getId()) + duration;
                double currentSoc = pathOfU.getSocOfNode(u.getId()) - consumption;

                if (currentSoc < 0) {
                    // Nicht genuegend Energie, um zu v zu gelangen

                    String lastStationId = pathOfU.getLastStation();

                    double lastStationChargingTime = 0.0;
                    double oldChargingTime = pathOfU.getChargingTimeOfNode(lastStationId);

                    double totalConsumption = 0;
                    boolean lastStationChargedEnough = true;

                    outerIf:
                    if (!lastStationId.isEmpty()) {
                        if (pathOfU.getSocOfNode(lastStationId) > maxSoc) {
                            lastStationChargedEnough = false;
                        }
                        // Falls letzte Ladestation existiert und noch nicht bis 100 % geladen wurde.
                        double additionalChargingTime = 0;
                        Node currentNode = u;
                        totalConsumption = consumption;
                        while (!currentNode.getId().equals(lastStationId)) {
                            String parentNodeId = pathOfU.getParentOfNode(currentNode.getId());
                            if (!parentNodeId.isEmpty()) {
                                totalConsumption += this.reader.getShortestEdgeBetweenNodes(parentNodeId, currentNode.getId()).getConsumption();
                            }
                            currentNode = this.reader.getNodeById(parentNodeId);
                            if (currentNode == null) break;
                        }

                        double necessarySoc = totalConsumption;

                        if (necessarySoc > maxSoc) {
                            lastStationChargedEnough = false;
                            oldChargingTime = 0.0;
                            break outerIf;
                        }

                        additionalChargingTime = this.calculateAdditionalChargeTime(pathOfU.getSocOfNode(lastStationId), totalConsumption, this.reader.getNodeById(lastStationId).getChargingPower());
                        lastStationChargingTime = oldChargingTime + additionalChargingTime;
                    }

                    if (lastStationId.isEmpty() || !lastStationChargedEnough) {
                        // Suche nach allen Ladestationen von u bis p oder Verbrauch zu groß wird, um Aufladen zu koennen.

                        totalConsumption = consumption;
                        Node currentNode = u;
                        Map<String, Double> lastStations = new HashMap<>();
                        while (currentNode != null) {
                            if (currentNode.getId().equals(lastStationId))
                                break;
                            if (currentNode.getChargingPower() > 0 && pathOfU.getSocOfNode(currentNode.getId()) < 100) {
                                lastStations.put(currentNode.getId(), totalConsumption);
                            }
                            String parentNodeId = pathOfU.getParentOfNode(currentNode.getId());
                            if (!parentNodeId.isEmpty()) {
                                totalConsumption += this.reader.getShortestEdgeBetweenNodes(parentNodeId, currentNode.getId()).getConsumption();
                            }
                            currentNode = this.reader.getNodeById(parentNodeId);
                        }

                        double chargingTimeNewLastStation = Double.MAX_VALUE;
                        for (Map.Entry<String, Double> lastStation : lastStations.entrySet()) {
                            String w = lastStation.getKey();
                            double necessarySoc = lastStation.getValue();
                            if (necessarySoc > maxSoc) {
                                break;
                            }
                            if (pathOfU.getSocOfNode(w) > necessarySoc) {
                                lastStationId = w;
                                break;
                            }
                            double tempChargingTimeNewLastStation = this.calculateAdditionalChargeTime(pathOfU.getSocOfNode(w), necessarySoc, this.reader.getNodeById(w).getChargingPower());
                            if (tempChargingTimeNewLastStation < chargingTimeNewLastStation) {
                                chargingTimeNewLastStation = tempChargingTimeNewLastStation;
                                lastStationId = w;
                                lastStationChargingTime = chargingTimeNewLastStation;
                                totalConsumption = lastStation.getValue();
                            }
                        }
                    }

                    if (lastStationId.isEmpty()) {
                        // v kann nicht ueber diesen Weg erreicht werden, da keine Ladestation nah genug dran liegt.
                        continue;
                    }

                    if (minChargingTime > lastStationChargingTime) {
                        lastStationChargingTime = 10.0;
                    }

                    double newTravelTimeV = currentTravelTime - oldChargingTime + lastStationChargingTime;

                    if (newTravelTimeV < pathOfNode.get(v.getId()).getTravelTimeOfNode(v.getId())) {
                        // Es wurde ein kuerzerer Weg gefunden: Alle Knoten von v bis p aktualisieren.
                        List<VisitedNode> visitedNodes = new ArrayList<>();

                        List<VisitedNode> visitedNodesFromU = pathOfU.getPath();
                        for (VisitedNode node : visitedNodesFromU) {
                            if (node.getId().equals(lastStationId)) {
                                break;
                            }
                            visitedNodes.add(node);
                        }

                        VisitedNode lastNodeBeforeStation = visitedNodes.get(visitedNodes.size() - 1);

                        double lastStationTravelTime = lastNodeBeforeStation.getTravelTime() + this.reader.getShortestEdgeBetweenNodes(lastNodeBeforeStation.getId(), lastStationId).getDuration() + lastStationChargingTime;
                        double lastStationSocWithoutCharging = lastNodeBeforeStation.getSoc() - this.reader.getShortestEdgeBetweenNodes(lastNodeBeforeStation.getId(), lastStationId).getConsumption();
                        double lastStationSocAfterCharging = this.calculateNewSoc(maxSoc, lastStationSocWithoutCharging, lastStationChargingTime, this.reader.getNodeById(lastStationId).getChargingPower());
                        VisitedNode visitedNodeLastStation = new VisitedNode(lastStationId, lastStationTravelTime, lastStationSocAfterCharging, lastStationChargingTime);
                        visitedNodes.add(visitedNodeLastStation);

                        VisitedNode visitedNodeV = new VisitedNode(v.getId(), newTravelTimeV, lastStationSocAfterCharging - totalConsumption, 0.0);

                        queue.put(v.getId(), newTravelTimeV);


                        if (!u.getId().equals(lastStationId)) {
                            Node currentNode = u;
                            VisitedNode visitedNodeU = new VisitedNode(u.getId(), visitedNodeV.getTravelTime() - duration, visitedNodeV.getSoc() + consumption, 0.0);

                            List<VisitedNode> visitedNodesFromLastStation = new ArrayList<>();

                            double currentNodeSoc = visitedNodeU.getSoc();

                            while (currentNode != null) {
                                String parentNodeId = pathOfU.getParentOfNode(currentNode.getId());
                                if (parentNodeId.equals(lastStationId) || parentNodeId.isEmpty()) break;
                                double newSoc = currentNodeSoc + this.reader.getShortestEdgeBetweenNodes(parentNodeId, currentNode.getId()).getConsumption();
                                double newTravelTime = pathOfU.getTravelTimeOfNode(parentNodeId) + lastStationChargingTime - oldChargingTime;
                                VisitedNode visitedNode = new VisitedNode(parentNodeId, newTravelTime, newSoc, 0.0);
                                visitedNodesFromLastStation.add(visitedNode);
                                currentNode = this.reader.getNodeById(parentNodeId);
                            }

                            for (int i = visitedNodesFromLastStation.size() - 1; i >= 0; i--) {
                                visitedNodes.add(visitedNodesFromLastStation.get(i));
                            }

                            visitedNodes.add(visitedNodeU);
                        }

                        visitedNodes.add(visitedNodeV);

                        Path path = new Path(visitedNodes);
                        pathOfNode.put(v.getId(), path);
                    }

                }
                else {
                    // Ausreichend Energie, um zu v zu gelangen

                    if (currentTravelTime < pathOfNode.get(v.getId()).getTravelTimeOfNode(v.getId())) {
                        List<VisitedNode> visitedNodes = new ArrayList<>(pathOfU.getPath());
                        VisitedNode visitedNodeV = new VisitedNode(v.getId(), currentTravelTime, currentSoc, 0.0);
                        visitedNodes.add(visitedNodeV);

                        Path path = new Path(visitedNodes);
                        pathOfNode.put(v.getId(), path);

                        queue.put(v.getId(), currentTravelTime);
                    }
                }
            }
        }

        return new AlgorithmResult(steps, 0.0, this.createShortestPathForResult(end), pathOfNode.get(end.getId()).getTravelTimeOfNode(end.getId()));
    }

    private void initialize(Node start, double initialCharge) {
        this.pathOfNode = new HashMap<>();

        List<String> nodeIds = this.reader.getAllNodeIds();
        for (String nodeId : nodeIds) {
            Path path = new Path(new ArrayList<>());
            pathOfNode.put(nodeId, path);
        }

        List<VisitedNode> startPath = new ArrayList<>();
        VisitedNode startNode = new VisitedNode(start.getId(), 0.0, initialCharge, 0.0);
        startPath.add(startNode);
        Path path = new Path(startPath);
        this.pathOfNode.put(startNode.getId(), path);
    }

    private Map<Node, Double> createShortestPathForResult(Node end) {
        Map<Node, Double> path = new LinkedHashMap<>();

        List<VisitedNode> visitedNodes = pathOfNode.get(end.getId()).getPath();

        for (VisitedNode visitedNode : visitedNodes) {
            Node node = this.reader.getNodeById(visitedNode.getId());
            path.put(node, visitedNode.getChargingTime());
        }


        return path;
    }

}
