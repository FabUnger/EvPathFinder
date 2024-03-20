package Logic;

import Data.AlgorithmResult;
import Data.Node;
import Persistence.GraphReader;

import java.util.List;

public class BusinessController implements Controller {

    private final Context context;
    private final GraphReader graphReader;

    public BusinessController(Context context, GraphReader graphReader) {
        this.context = context;
        this.graphReader = graphReader;
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
        return this.context.executeAlgorithm(start, end, maxSoc, initialCharge, minChargingTime);
    }
}
