package Persistence;

import Data.Graph;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

public class Neo4jReader implements GraphReader {

    @Override
    public Graph GetGraph() {

        // TODO: URL und AuthToken anpassen
        Driver driver = GraphDatabase.driver("bolt://localhost:XXXX", AuthTokens.basic("username", "password"));
        Graph graph = new Graph();

        try (Session session = driver.session()) {

        } finally {
            driver.close();
        }

        return null;
    }
}
