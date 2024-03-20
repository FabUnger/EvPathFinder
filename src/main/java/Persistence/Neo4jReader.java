package Persistence;

import Data.Edge;
import Data.Node;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.Values.parameters;

public class Neo4jReader implements GraphReader {

    private final Driver driver;

    public Neo4jReader(String uri, String user, String password) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }


    @Override
    public Node getNodeById(String nodeId) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run("MATCH (n) WHERE n.id = $id RETURN n.id AS id, n.chargingPower AS chargingPower",
                        parameters("id", nodeId));
                if (result.hasNext()) {
                    Record record = result.single();
                    return new Node(record.get("id").asString(), record.get("chargingPower").asInt());
                }
                return null;
            });
        } catch (Exception e) {
            System.err.println("Fehler beim Abrufen des Knotens: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Edge> getEdgesFromSourceNode(String nodeId) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run("MATCH (n {id: $id})-[r]->(m) RETURN m.id AS destinationId, r.duration AS duration, r.consumption AS consumption",
                        parameters("id", nodeId));
                List<Edge> edges = new ArrayList<>();
                while (result.hasNext()) {
                    Record record = result.next();
                    edges.add(new Edge(nodeId, record.get("destinationId").asString(),
                            record.get("duration").asInt(), record.get("consumption").asFloat()));
                }
                return edges;
            });
        } catch (Exception e) {
            System.err.println("Fehler beim Abrufen der Kanten vom Ursprungsknoten: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Edge getShortestEdgeBetweenNodes(String sourceId, String destinationId) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run(
                              "MATCH (start {id: $sourceId})-[r]->(end {id: $destinationId}) " +
                                 "RETURN start.id AS sourceId, end.id AS destinationId, r.duration AS duration, r.consumption AS consumption " +
                                 "ORDER BY r.duration ASC LIMIT 1",
                        parameters("sourceId", sourceId, "destinationId", destinationId));
                if (result.hasNext()) {
                    Record record = result.single();
                    return new Edge(
                            record.get("sourceId").asString(),
                            record.get("destinationId").asString(),
                            record.get("duration").asInt(),
                            record.get("consumption").asFloat()
                    );
                }
                return null;
            });
        } catch (Exception e) {
            System.err.println("Fehler beim Abrufen der kürzesten Kante zwischen Knoten: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<String> getAllNodeIds() {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run("MATCH (n) RETURN n.id AS id");
                List<String> nodeIds = new ArrayList<>();
                while (result.hasNext()) {
                    Record record = result.next();
                    nodeIds.add(record.get("id").asString());
                }
                return nodeIds;
            });
        } catch (Exception e) {
            System.err.println("Fehler beim Abrufen aller Knoten-IDs: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean isNodeExisting(String id) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run("MATCH (n) WHERE n.id = $id RETURN count(n) > 0 AS exists",
                        parameters("id", id));
                return result.single().get("exists").asBoolean();
            });
        } catch (Exception e) {
            System.err.println("Fehler beim Überprüfen, ob ein Knoten existiert: " + e.getMessage());
            return false;
        }
    }
}
