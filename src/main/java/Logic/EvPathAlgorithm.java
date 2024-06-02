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
        type = AlgorithmType.DIBASEM;
    }

    @Override
    public AlgorithmType getType() {
        return this.type;
    }

    @Override
    protected AlgorithmResult executeAlgorithm(Node start, Node end, double maxSoc, double initialCharge, int minChargingTime) {
        PriorityQueue queue = new PriorityQueue();

        // Initialisierung des Algorithmus
        this.initialize(queue, start, initialCharge);

        Path result = null;

        int steps = 0;

        while (!queue.isEmpty()) {
            steps++;

            // Aktuell bearbeiteter Knoten = u aus queue holen
            Path pathOfU = this.pathOfNode.get(queue.poll());
            VisitedNode u = pathOfU.getLastNode();

            // Falls u der Zielknoten ist, wird while-Schleife beendet
            if (u.getId().getName().equals(end.getId())) {
                // Endknoten gefunden. Dessen Nachbarn muessen nicht mehr ueberprueft werden.
                result = pathOfU;
                break;
            }

            // edgesFromU = Alle Kanten, die von u ausgehen
            List<Edge> edgesFromU = this.reader.getEdgesFromSourceNode(u.getId().getName());

            // Fuer alle Kanten, die von u ausgehen, folgende Schleife durchgehen
            for (Edge edgeFromU : edgesFromU) {
                // v = aktueller Nachbar dieses Schleifendurchlaufs
                Node v = this.reader.getNodeById(edgeFromU.getDestinationId());


                // speichern der Eigenschaften der Kante in Variablen
                double duration = edgeFromU.getDuration();
                double consumption = edgeFromU.getConsumption();

                // Berechnungen der entsprechenden Reisezeit und des Ladestands bei v
                double currentTravelTime = pathOfU.getTravelTimeOfNode(u.getId()) + duration;
                double currentSoc = pathOfU.getSocOfNode(u.getId()) - consumption;

                // Ueberpruefung, ob der Ladestand bei v groesser oder kleiner als 0 ist
                if (currentSoc < 0) {
                    // Ladestand bei v kleiner als 0, nicht genuegend Energie vorhanden, um nach jetzigem Stand erreichen zu koennen

                    // Letzte Ladestation erhalten und Initialwerte fuer die darauffolgenden Ueberpruefungen setzen
                    VisitedNodeId lastStationId = pathOfU.getLastStation();
                    double lastStationChargingTime = 0.0;
                    double oldChargingTime = 0.0;
                    double totalConsumption = 0.0;
                    boolean lastStationChargedEnough = true;

                    // Ueberpruefung, ob bisher bei einer Ladestation geladen wurde, d.h. ob eine Ladestation gefunden wurde
                    if (lastStationId != null) {
                        // Falls bereits bei einer Ladestation geladen wurde
                        if (pathOfU.getSocOfNode(lastStationId) >= maxSoc) {
                            // Falls der Ladestand bei dieser Ladestation bereits auf 100 % ist, setze Flag, dass spaeter eine weitere Ladestation gefunden werden muss
                            lastStationChargedEnough = false;
                        }

                        // Gehe von jetzigem Knoten u bis zur Ladestation durch, um den Gesamtverbrauch von der Ladestation bis v zu berechnen und daraus die notwendige Ladezeit zu bestimmen
                        VisitedNodeId currentNodeId = u.getId();
                        totalConsumption = consumption;
                        while (currentNodeId != null && !currentNodeId.equals(lastStationId)) {
                            VisitedNodeId parentNodeId = pathOfU.getParentOfNode(currentNodeId);
                            if (parentNodeId != null) {
                                totalConsumption += this.reader.getShortestEdgeBetweenNodes(parentNodeId.getName(), currentNodeId.getName()).getConsumption();
                            }
                            currentNodeId = parentNodeId;
                        }

                        // Ueberprufung, ob der Gesamtverbrauch groesser als die maximale Akkukapazitaet ist
                        if (totalConsumption > maxSoc) {
                            // Falls der Gesamtverbrauch groeßer als die maximale Akkukapazitaet ist, reicht das Laden bei dieser Ladestation nicht aus, um v erreichen zu koennen
                            lastStationChargedEnough = false;
                            oldChargingTime = 0.0;
                        } else {
                            // Laden bei dieser Ladestation reicht aus, um v erreichen zu koennen, weshalb die neue Ladezeit fuer diese Ladestation berechnet und in einer Variable gespeichert wird
                            double socWithOutCharging = pathOfU.getSocOfNode(pathOfU.getParentOfNode(lastStationId)) - this.reader.getShortestEdgeBetweenNodes(pathOfU.getParentOfNode(lastStationId).getName(), lastStationId.getName()).getConsumption();
                            lastStationChargingTime = this.calculateAdditionalChargeTime(socWithOutCharging, totalConsumption, this.reader.getNodeById(lastStationId.getName()).getChargingPower());
                            // speichere die bisherige Ladezeit der Ladestation in einer Variable
                            oldChargingTime = pathOfU.getChargingTimeOfNode(lastStationId);
                        }
                    }

                    boolean newLastStationAdded = false;
                    // Ueberpruefung, ob eine Ladestation gefunden wurde, oder ob bei einer bereits geladenen Ladestation ausreichend nachgeladen wurde
                    if (lastStationId == null || !lastStationChargedEnough) {
                        // Bisher keine Ladestation gefunden oder es konnte bei einer bereits geladenen Ladestation nicht ausreichend zusaetzlich geladen werden

                        // Suche nach allen Ladestationen von u bis p und speichere diese in lastStations mit zu ladender Energiemenge
                        // Falls der Verbrauch groeßer als die maximale Akkukapazitaet wird, kann die Suche abgebrochen werden
                        totalConsumption = consumption;
                        VisitedNodeId currentNodeId = u.getId();
                        Map<VisitedNodeId, Double> lastStations = new HashMap<>();
                        while (currentNodeId != null && !currentNodeId.equals(lastStationId)) {
                            if (this.reader.getNodeById(currentNodeId.getName()).getChargingPower() > 0 && totalConsumption < maxSoc) {
                                // Falls dieser Knoten eine Ladestation ist und noch ausreichend Energie dort geladen werden kann, fuege diese Ladestation zur Auswahl hinzu
                                lastStations.put(currentNodeId, totalConsumption);
                            }
                            // Erhalte Parent von diesem Knoten fuer weitere Suche und falls Parent vorhanden, addiere den Kantenverbrauch zum Gesamtverbrauch hinzu
                            VisitedNodeId parentNodeId = pathOfU.getParentOfNode(currentNodeId);
                            if (parentNodeId != null) {
                                totalConsumption += this.reader.getShortestEdgeBetweenNodes(parentNodeId.getName(), currentNodeId.getName()).getConsumption();
                            }
                            currentNodeId = parentNodeId;
                        }

                        lastStationId = null;
                        // Gehe gefundene Ladestationen durch
                        double newChargingTimeLastStation = Double.MAX_VALUE;
                        for (Map.Entry<VisitedNodeId, Double> lastStation : lastStations.entrySet()) {
                            VisitedNodeId w = lastStation.getKey();
                            double necessarySoc = lastStation.getValue();
                            if (necessarySoc > maxSoc) {
                                // Falls Gesamtverbrauch (notwendige Energiemenge) groesser als die maximale Akkukapazitaet ist, kann bei dieser und allen folgenden Ladestation nicht geladen werden
                                break;
                            }
                            if (pathOfU.getSocOfNode(w) > necessarySoc) {
                                // Falls bei dieser Ladestation der Ladestand bereits groesser als die notwendige Energiemenge ist, wird direkt diese Ladestation gewaehlt
                                lastStationId = w;
                                newLastStationAdded = true;
                                break;
                            }
                            // Berechne die neue Ladezeit bei dieser Ladestation
                            double tempNewChargingTimeLastStation = this.calculateAdditionalChargeTime(pathOfU.getSocOfNode(w), necessarySoc, this.reader.getNodeById(w.getName()).getChargingPower());
                            if (tempNewChargingTimeLastStation < newChargingTimeLastStation) {
                                // Falls die Ladezeit der aktuellen Ladestation kleiner ist, als die bisher beste Ladezeit, wird diese Ladestation als neue beste Ladestation gewaehlt
                                newChargingTimeLastStation = tempNewChargingTimeLastStation;
                                lastStationId = w;
                                lastStationChargingTime = newChargingTimeLastStation;
                                totalConsumption = necessarySoc;
                                newLastStationAdded = true;
                            }
                        }
                    }

                    if (lastStationId == null) {
                        // Es konnte keine Ladestation gefunden werden, weshalb v ueber diesen Weg nicht erreichbar ist
                        continue;
                    }

                    if (minChargingTime > lastStationChargingTime && pathOfU.getNodeById(lastStationId).getSoc() < totalConsumption) {
                        // Falls die berechnete Ladestation kleiner als die gewuenschte Minimalladezeit ist und tatsaechlich geladen werden muss, dann setze die Ladezeit auf die gewuenschte Ladezeit
                        lastStationChargingTime = minChargingTime;
                    }


                    // Erstelle den neuen Weg nach v
                    List<VisitedNode> visitedNodes = new ArrayList<>();

                    int lastStationIndex = -1;
                    VisitedNodeId oldLastStationId = null;
                    // Speichere alle Knoten nach u in einer separaten Liste
                    List<VisitedNode> visitedNodesFromU = pathOfU.getPath();
                    // ID von der letzten Ladestation erhalten
                    for (int i = visitedNodesFromU.size() - 1; i >= 0; i--) {
                        if (visitedNodesFromU.get(i).getChargingTime() > 0.0) {
                            lastStationIndex = i;
                            oldLastStationId = visitedNodesFromU.get(i).getId();
                            break;
                        }
                    }
                    // Ueberpruefe, ob bisher eine Ladestation existiert hat und ob eine neue Ladestation hinzugefuegt wurde
                    if (lastStationIndex != -1 && newLastStationAdded) {
                        // Falls bisher eine Ladestation existiert hat und eine neue Ladestation hinzugefuegt wurde
                        // fuege alle Knoten von Start bis zu der zuletzt geladenen Ladestation zum Weg hinzu
                        for (int i = 0; i < lastStationIndex; i++) {
                            visitedNodes.add(visitedNodesFromU.get(i));
                        }
                        // Berechne Verbrauch von bisher letzter Ladestation zu neuer Ladestation
                        double consumptionFromOldStationToNewStation = 0.0;
                        for (int i = lastStationIndex; i < visitedNodesFromU.size() - 1; i++) {
                            consumptionFromOldStationToNewStation += this.reader.getShortestEdgeBetweenNodes(visitedNodesFromU.get(i).getId().getName(), visitedNodesFromU.get(i+1).getId().getName()).getConsumption();
                            if (visitedNodesFromU.get(i + 1).getId().equals(lastStationId)) {
                                break;
                            }
                        }
                        // Erstelle Werte fuer die Ladestation bei der bisher zuletzt geladen wurde und erstelle einen neuen VisitedNode fuer diese und fuege sie zum Weg hinzu
                        VisitedNode lastNodeBeforeLastStation = visitedNodesFromU.get(lastStationIndex - 1);
                        double oldLastStationChargingPower = this.reader.getNodeById(oldLastStationId.getName()).getChargingPower();
                        double oldLastStationSocWithoutCharging = lastNodeBeforeLastStation.getSoc() - this.reader.getShortestEdgeBetweenNodes(lastNodeBeforeLastStation.getId().getName(), visitedNodesFromU.get(lastStationIndex).getId().getName()).getConsumption();
                        double oldLastStationChargingTime = this.calculateAdditionalChargeTime(oldLastStationSocWithoutCharging, consumptionFromOldStationToNewStation, oldLastStationChargingPower);
                        if (oldLastStationChargingTime < minChargingTime) {
                            oldLastStationChargingTime = minChargingTime;
                        }
                        double oldLastStationTravelTime = lastNodeBeforeLastStation.getTravelTime() + this.reader.getShortestEdgeBetweenNodes(lastNodeBeforeLastStation.getId().getName(), visitedNodesFromU.get(lastStationIndex).getId().getName()).getDuration() + oldLastStationChargingTime;
                        double oldLastStationSocAfterCharging = this.calculateNewSoc(maxSoc, oldLastStationSocWithoutCharging, oldLastStationChargingTime, oldLastStationChargingPower);
                        VisitedNode oldLastStation = new VisitedNode(oldLastStationId.getName(), oldLastStationTravelTime, oldLastStationSocAfterCharging, oldLastStationChargingTime);
                        visitedNodes.add(oldLastStation);
                        // Fuege alle Knoten von der zuletzt geladenen Ladestation zur neu hinzgefuegten Ladestation zum Weg hinzu
                        for (int i = lastStationIndex; i < visitedNodesFromU.size() - 1; i++) {
                            VisitedNode node = visitedNodes.get(i);
                            VisitedNode successor = visitedNodesFromU.get(i + 1);
                            if (successor.getId().equals(lastStationId)) {
                                break;
                            }
                            double edgeConsumption = this.reader.getShortestEdgeBetweenNodes(node.getId().getName(), successor.getId().getName()).getConsumption();
                            double edgeDuration = this.reader.getShortestEdgeBetweenNodes(node.getId().getName(), successor.getId().getName()).getDuration();
                            VisitedNode visitedNode = new VisitedNode(successor.getId().getName(), node.getTravelTime() + edgeDuration, node.getSoc() - edgeConsumption, successor.getChargingTime());
                            visitedNodes.add(visitedNode);
                        }
                    } else {
                        // Falls bisher keine Ladestation existiert hat oder keine neue Ladestation hinzugefuegt wurde
                        // Uebernehme alle Knoten start bis zur neuen Ladestation von dem Weg nach u
                        for (VisitedNode node : visitedNodesFromU) {
                            if (node.getId().equals(lastStationId)) {
                                break;
                            }
                            visitedNodes.add(node);
                        }
                    }

                    VisitedNode lastNodeBeforeStation = visitedNodes.get(visitedNodes.size() - 1);

                    // Erstelle ein neues VisitedNode-Objekt fuer die neue Ladestation
                    double lastStationTravelTime = lastNodeBeforeStation.getTravelTime() + this.reader.getShortestEdgeBetweenNodes(lastNodeBeforeStation.getId().getName(), lastStationId.getName()).getDuration() + lastStationChargingTime;
                    double lastStationSocWithoutCharging = lastNodeBeforeStation.getSoc() - this.reader.getShortestEdgeBetweenNodes(lastNodeBeforeStation.getId().getName(), lastStationId.getName()).getConsumption();
                    double lastStationSocAfterCharging = this.calculateNewSoc(maxSoc, lastStationSocWithoutCharging, lastStationChargingTime, this.reader.getNodeById(lastStationId.getName()).getChargingPower());
                    VisitedNode visitedNodeLastStation = new VisitedNode(lastStationId.getName(), lastStationTravelTime, lastStationSocAfterCharging, lastStationChargingTime);
                    visitedNodes.add(visitedNodeLastStation);

                    double newSocV = lastStationSocAfterCharging - totalConsumption;

                    // Fuege alle Knoten mit den angepassten Werten von der Ladestation an bis einschließlich u zur Liste hinzu
                    for (int i = visitedNodes.size() - 1; i < visitedNodesFromU.size() - 1; i++) {
                        VisitedNode node = visitedNodes.get(i);
                        VisitedNode successor = visitedNodesFromU.get(i + 1);
                        double  edgeConsumption = this.reader.getShortestEdgeBetweenNodes(node.getId().getName(), successor.getId().getName()).getConsumption();
                        double edgeDuration = this.reader.getShortestEdgeBetweenNodes(node.getId().getName(), successor.getId().getName()).getDuration();
                        VisitedNode visitedNode = new VisitedNode(successor.getId().getName(), node.getTravelTime() + edgeDuration, node.getSoc() - edgeConsumption, successor.getChargingTime());
                        visitedNodes.add(visitedNode);
                    }

                    // Erstelle ein neues VisitedNode-Objekt fuer v
                    VisitedNode newU = visitedNodes.get(visitedNodes.size() - 1);
                    // Berechne die neue Reisezeit von Start nach v
                    double newTravelTimeV = newU.getTravelTime() + this.reader.getShortestEdgeBetweenNodes(newU.getId().getName(), v.getId()).getDuration();
                    VisitedNode visitedNodeV = new VisitedNode(v.getId(), newTravelTimeV, newSocV, 0.0);

                    // Ueberpruefe, ob der neue Zustand von v schlechter als irgendein anderer Zustand in V ist
                    if (this.checkIfCurrentNodeIsBetter(visitedNodeV)) {
                        // Vervollstaendige die Liste durch Hinzufuegen von v und erstelle ein Path-Objekt und fuege dieses zu pathOfNode hinzu, sowie den VisitedNode von v zur Queue
                        visitedNodes.add(visitedNodeV);
                        Path path = new Path(visitedNodes);
                        pathOfNode.put(visitedNodeV.getId(), path);
                        queue.put(visitedNodeV.getId(), newTravelTimeV);
                    }
                }
                else {
                    // Ladestand bei v groesser als 0, genuegend Energie vorhanden, um v erreichen zu koennen, sodass nicht geladen werden muss

                    // Uebernehme den Pfad nach u
                    List<VisitedNode> visitedNodes = new ArrayList<>(pathOfU.getPath());
                    // Erstelle ein VisitedNode-Objekt fuer v, fuege dieses sowohl zur Liste fuer den neuen Pfad als auch zur Queue hinzu
                    VisitedNode visitedNodeV = new VisitedNode(v.getId(), currentTravelTime, currentSoc, 0.0);
                    // Ueberpruefe, ob der neue Zustand von v schlechter als irgendein anderer Zustand in V ist
                    if (this.checkIfCurrentNodeIsBetter(visitedNodeV)) {
                        visitedNodes.add(visitedNodeV);
                        queue.put(visitedNodeV.getId(), currentTravelTime);
                        // Erstelle einen neuen Pfad fuer den Knoten v
                        Path path = new Path(visitedNodes);
                        pathOfNode.put(visitedNodeV.getId(), path);
                    }
                }
            }
        }

        // Falls kein Ergebnis gefunden wurde, gebe einen leeren Pfad und die entsprechenden Analysedaten zurueck
        if (result == null) {
            result = new Path(new ArrayList<>());
            return new AlgorithmResult(steps, 0.0, result, 0.0);
        }
        // Falls ein Pfad gefunden wurde, gebe diesen in einem AlgorithmResult-Objekt mit den entsprechenden Analysedaten zureuck
        double travelTime = result.getTravelTimeOfNode(result.getLastNode().getId());
        return new AlgorithmResult(steps, 0.0, result, travelTime);
    }

    private void initialize(PriorityQueue queue, Node start, double initialCharge) {
        this.pathOfNode = new HashMap<>();

        // Erstelle den Pfad für den Startknoten (besteht nur aus diesem selbst)
        List<VisitedNode> startPath = new ArrayList<>();
        VisitedNode startNode = new VisitedNode(start.getId(), 0.0, initialCharge, 0.0);
        // Fuege Startknoten zur Queue hinzu
        queue.put(startNode.getId(), 0.0);
        startPath.add(startNode);
        Path path = new Path(startPath);
        // Fuege Pfad des Startknotens zu allen paths hinzu
        this.pathOfNode.put(startNode.getId(), path);
    }

    private boolean checkIfCurrentNodeIsBetter(VisitedNode visitedNode) {
        for (Map.Entry<VisitedNodeId, Path> entry : pathOfNode.entrySet()) {
            VisitedNodeId nodeId = entry.getKey();
            Path path = entry.getValue();

            // Prüfen, ob der Name des aktuellen Knotens mit dem Namen des Knotens im Path übereinstimmt
            if (nodeId.getName().equals(visitedNode.getId().getName())) {
                VisitedNode nodeInPath = path.getNodeById(nodeId);

                // Wenn der Knoten gefunden wurde, vergleichen Sie die Reisezeit und den SOC
                if (nodeInPath != null) {
                    if (visitedNode.getTravelTime() > nodeInPath.getTravelTime() && visitedNode.getSoc() < nodeInPath.getSoc()) {
                        // Der aktuelle Knoten ist schlechter oder gleich in Bezug auf Reisezeit und SOC
                        return false;
                    }
                    // else: Der aktuelle Knoten ist besser in Bezug auf Reisezeit und SOC
                }
            }
        }
        // Der aktuelle Knoten ist besser als alle Knoten mit demselben Namen im Path
        return true;
    }

}
