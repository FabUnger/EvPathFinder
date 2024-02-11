package Persistence;

import Data.Edge;
import Data.Graph;
import Data.Node;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Neo4jReader implements GraphReader {

    private final Driver driver;

    public Neo4jReader(String uri, String user, String password) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }


    @Override
    public Graph getGraph() {
        return null;
    }

    @Override
    public Node getNodeById(String nodeId) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                var result = tx.run("MATCH (n) WHERE n.id = $id RETURN n.id as id, exists(n.chargingStation) AS hasChargingStation",
                        parameters("id", nodeId));
                if (result.hasNext()) {
                    var record = result.single();
                    return new Node(record.get("id").asString(), record.get("hasChargingStation").asBoolean());
                }
                return null;
            });
        }
    }

    @Override
    public List<Edge> getEdgesFromNode(String nodeId) {
        List<Edge> edges = new ArrayList<>();
        try (Session session = driver.session()) {
            session.readTransaction(tx -> {
                var result = tx.run("MATCH (n {id: $id})-[r]->(m) RETURN n.id AS sourceId, m.id AS destinationId, r.duration AS duration, r.consumption AS consumption",
                        parameters("id", nodeId));
                result.list().forEach(record -> {
                    Node source = new Node(record.get("sourceId").asString(), false); // Das zweite Argument ist ein Platzhalter
                    Node destination = new Node(record.get("destinationId").asString(), false); // Das zweite Argument ist ein Platzhalter
                    edges.add(new Edge(source, destination, record.get("duration").asDouble(), record.get("consumption").asDouble()));
                });
                return null;
            });
        }
        return edges;
    }

    @Override
    public List<Node> getAllNodes() {
        List<Node> nodes = new ArrayList<>();
        try (Session session = driver.session()) {
            session.readTransaction(tx -> {
                var result = tx.run("MATCH (n) RETURN n.id AS id, exists(n.chargingStation) AS hasChargingStation");
                result.list().forEach(record -> nodes.add(new Node(record.get("id").asString(), record.get("hasChargingStation").asBoolean())));
                return null;
            });
        }
        return nodes;
    }

    private static Map<String, Object> parameters(String key, Object value) {
        return Collections.singletonMap(key, value);
    }
}
