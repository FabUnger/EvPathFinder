package Logic;

import Data.AlgorithmResult;
import Data.Node;
import Persistence.GraphReader;
import Persistence.Properties;

import java.util.List;

public class BusinessController implements Controller {

    private final PathAlgorithmContext context;
    private final GraphReader graphReader;

    public BusinessController(PathAlgorithmContext context, GraphReader graphReader) {
        this.context = context;
        this.graphReader = graphReader;
    }

    @Override
    public void initializeDatabase(Properties properties) {
        this.graphReader.setProperties(properties);
    }

    @Override
    public List<String> getAllNodeIds() {
        List<String> ids = this.graphReader.getAllNodeIds();
        return ids;
    }

    @Override
    public boolean isNodeExisting(String id) {
        return this.graphReader.isNodeExisting(id);
    }

    @Override
    public void selectAlgorithm(AlgorithmType type) {
        this.context.setPathAlgorithm(type);
    }

    @Override
    public AlgorithmResult executeAlgorithm(String startId, String endId, double maxSoc, double initialCharge, int minChargingTime) {
        Node start = this.graphReader.getNodeById(startId);
        Node end = this.graphReader.getNodeById(endId);
        if (start == null) {
            System.err.println("Fehler beim Verbindungsaufbau mit Datenbank. Bitte Datenbank oder Anmeldedaten ueberpruefen.");
            return null;
        }
        return this.context.executeAlgorithm(start, end, maxSoc, initialCharge, minChargingTime);
    }

    @Override
    public String getSelectedAlgorithmTypeAsString() {
        return this.context.getSelectedTypeAsString();
    }
}
