package Logic;

import Data.*;
import Data.PriorityQueue;
import Persistence.GraphReader;

import java.util.*;

public class EvPathAlgorithm extends PathAlgorithm {

    private final AlgorithmType type;

    private Map<VisitedNodeId, Path> pathOfNode;

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
        PriorityQueue queue = new PriorityQueue();
        this.initialize(queue, start, initialCharge);

        Path result = null;

        int steps = 0;

        while (!queue.isEmpty()) {
            steps++;

            Path pathOfU = this.pathOfNode.get(queue.poll());
            VisitedNode u = pathOfU.getLastNode();


            if (u.getId().getName().equals(end.getId())) {
                // Endknoten gefunden. Dessen Nachbarn muessen nicht mehr ueberprueft werden.
                result = pathOfU;
                break;
            }

            List<Edge> edgesFromU = this.reader.getEdgesFromSourceNode(u.getId().getName());
            for (Edge edgeFromU : edgesFromU) {
                Node v = this.reader.getNodeById(edgeFromU.getDestinationId());

                double duration = edgeFromU.getDuration();
                double consumption = edgeFromU.getConsumption();

                double currentTravelTime = pathOfU.getTravelTimeOfNode(u.getId()) + duration;
                double currentSoc = pathOfU.getSocOfNode(u.getId()) - consumption;

                if (currentSoc < 0) {
                    // Nicht genuegend Energie, um zu v zu gelangen

                    VisitedNodeId lastStationId = pathOfU.getLastStation();

                    double lastStationChargingTime = 0.0;
                    double oldChargingTime = pathOfU.getChargingTimeOfNode(lastStationId);

                    double totalConsumption = 0;
                    boolean lastStationChargedEnough = true;

                    if (lastStationId != null) {
                        if (pathOfU.getSocOfNode(lastStationId) > maxSoc) {
                            lastStationChargedEnough = false;
                        }
                        // Falls letzte Ladestation existiert und noch nicht bis 100 % geladen wurde.
                        VisitedNodeId currentNodeId = u.getId();
                        totalConsumption = consumption;
                        while (currentNodeId != null && !currentNodeId.equals(lastStationId)) {
                            VisitedNodeId parentNodeId = pathOfU.getParentOfNode(currentNodeId);
                            if (parentNodeId != null) {
                                totalConsumption += this.reader.getShortestEdgeBetweenNodes(parentNodeId.getName(), currentNodeId.getName()).getConsumption();
                            }
                            currentNodeId = parentNodeId;
                        }

                        double necessarySoc = totalConsumption;

                        if (necessarySoc > maxSoc) {
                            lastStationChargedEnough = false;
                            oldChargingTime = 0.0;
                        } else {
                            double additionalChargingTime = this.calculateAdditionalChargeTime(pathOfU.getSocOfNode(lastStationId), totalConsumption, this.reader.getNodeById(lastStationId.getName()).getChargingPower());
                            lastStationChargingTime = oldChargingTime + additionalChargingTime;
                        }
                    }

                    if (lastStationId == null || !lastStationChargedEnough) {
                        // Suche nach allen Ladestationen von u bis p oder Verbrauch zu groß wird, um Aufladen zu koennen.

                        totalConsumption = consumption;
                        VisitedNodeId currentNodeId = u.getId();
                        Map<VisitedNodeId, Double> lastStations = new HashMap<>();
                        while (currentNodeId != null) {
                            if (currentNodeId.equals(lastStationId))
                                break;
                            if (this.reader.getNodeById(currentNodeId.getName()).getChargingPower() > 0 && pathOfU.getSocOfNode(currentNodeId) < 100) {
                                lastStations.put(currentNodeId, totalConsumption);
                            }
                            VisitedNodeId parentNodeId = pathOfU.getParentOfNode(currentNodeId);
                            if (parentNodeId != null) {
                                totalConsumption += this.reader.getShortestEdgeBetweenNodes(parentNodeId.getName(), currentNodeId.getName()).getConsumption();
                            }
                            currentNodeId = parentNodeId;
                        }

                        double newChargingTimeLastStation = Double.MAX_VALUE;
                        for (Map.Entry<VisitedNodeId, Double> lastStation : lastStations.entrySet()) {
                            VisitedNodeId w = lastStation.getKey();
                            double necessarySoc = lastStation.getValue();
                            if (necessarySoc > maxSoc) {
                                break;
                            }
                            if (pathOfU.getSocOfNode(w) > necessarySoc) {
                                lastStationId = w;
                                break;
                            }
                            double tempNewChargingTimeLastStation = this.calculateAdditionalChargeTime(pathOfU.getSocOfNode(w), necessarySoc, this.reader.getNodeById(w.getName()).getChargingPower());
                            if (tempNewChargingTimeLastStation < newChargingTimeLastStation) {
                                newChargingTimeLastStation = tempNewChargingTimeLastStation;
                                lastStationId = w;
                                lastStationChargingTime = newChargingTimeLastStation;
                                totalConsumption = necessarySoc;
                            }
                        }
                    }

                    if (lastStationId == null) {
                        // v kann nicht ueber diesen Weg erreicht werden, da keine Ladestation nah genug dran liegt.
                        continue;
                    }

                    if (minChargingTime > lastStationChargingTime) {
                        lastStationChargingTime = minChargingTime;
                    }

                    double newTravelTimeV = currentTravelTime - oldChargingTime + lastStationChargingTime;

                    List<VisitedNode> visitedNodes = new ArrayList<>();

                    List<VisitedNode> visitedNodesFromU = pathOfU.getPath();
                    for (VisitedNode node : visitedNodesFromU) {
                        if (node.getId().equals(lastStationId)) {
                            break;
                        }
                        visitedNodes.add(node);
                    }

                    VisitedNode lastNodeBeforeStation = visitedNodes.get(visitedNodes.size() - 1);

                    double lastStationTravelTime = lastNodeBeforeStation.getTravelTime() + this.reader.getShortestEdgeBetweenNodes(lastNodeBeforeStation.getId().getName(), lastStationId.getName()).getDuration() + lastStationChargingTime;
                    double lastStationSocWithoutCharging = lastNodeBeforeStation.getSoc() - this.reader.getShortestEdgeBetweenNodes(lastNodeBeforeStation.getId().getName(), lastStationId.getName()).getConsumption();
                    double lastStationSocAfterCharging = this.calculateNewSoc(maxSoc, lastStationSocWithoutCharging, lastStationChargingTime, this.reader.getNodeById(lastStationId.getName()).getChargingPower());
                    VisitedNode visitedNodeLastStation = new VisitedNode(lastStationId.getName(), lastStationTravelTime, lastStationSocAfterCharging, lastStationChargingTime);
                    visitedNodes.add(visitedNodeLastStation);

                    VisitedNode visitedNodeV = new VisitedNode(v.getId(), newTravelTimeV, lastStationSocAfterCharging - totalConsumption, 0.0);

                    queue.put(visitedNodeV.getId(), newTravelTimeV);


                    if (!u.getId().equals(lastStationId)) {
                        VisitedNodeId currentNodeId = u.getId();
                        VisitedNode visitedNodeU = new VisitedNode(u.getId().getName(), visitedNodeV.getTravelTime() - duration, visitedNodeV.getSoc() + consumption, 0.0);

                        List<VisitedNode> visitedNodesFromLastStation = new ArrayList<>();

                        double currentNodeSoc = visitedNodeU.getSoc();

                        while (currentNodeId != null) {
                            VisitedNodeId parentNodeId = pathOfU.getParentOfNode(currentNodeId);
                            if (parentNodeId == null || parentNodeId == lastStationId) break;
                            currentNodeSoc += this.reader.getShortestEdgeBetweenNodes(parentNodeId.getName(), currentNodeId.getName()).getConsumption();
                            double newTravelTime = pathOfU.getTravelTimeOfNode(parentNodeId) + lastStationChargingTime - oldChargingTime;
                            VisitedNode visitedNode = new VisitedNode(parentNodeId.getName(), newTravelTime, currentNodeSoc, 0.0);
                            visitedNodesFromLastStation.add(visitedNode);
                            currentNodeId = parentNodeId;
                        }

                        for (int i = visitedNodesFromLastStation.size() - 1; i >= 0; i--) {
                            visitedNodes.add(visitedNodesFromLastStation.get(i));
                        }

                        visitedNodes.add(visitedNodeU);
                    }

                    visitedNodes.add(visitedNodeV);

                    Path path = new Path(visitedNodes);
                    pathOfNode.put(visitedNodeV.getId(), path);

                }
                else {
                    // Ausreichend Energie, um zu v zu gelangen

                    List<VisitedNode> visitedNodes = new ArrayList<>(pathOfU.getPath());
                    VisitedNode visitedNodeV = new VisitedNode(v.getId(), currentTravelTime, currentSoc, 0.0);

                    queue.put(visitedNodeV.getId(), currentTravelTime);

                    visitedNodes.add(visitedNodeV);

                    Path path = new Path(visitedNodes);
                    pathOfNode.put(visitedNodeV.getId(), path);
                }
            }
        }

        if (result == null) {
            return null;
        }
        double travelTime = result.getTravelTimeOfNode(result.getLastNode().getId());
        return new AlgorithmResult(steps, 0.0, result, travelTime);
    }

    private void initialize(PriorityQueue queue, Node start, double initialCharge) {
        this.pathOfNode = new HashMap<>();


        List<VisitedNode> startPath = new ArrayList<>();
        VisitedNode startNode = new VisitedNode(start.getId(), 0.0, initialCharge, 0.0);
        queue.put(startNode.getId(), 0.0);
        startPath.add(startNode);
        Path path = new Path(startPath);
        this.pathOfNode.put(startNode.getId(), path);
    }

}
