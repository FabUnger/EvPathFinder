package Logic;

import Data.AlgorithmResult;
import Data.Graph;
import Persistence.GraphReader;
import Persistence.Neo4jReader;

public class BusinessController implements Controller {

    private final Context context;
    private final GraphReader graphReader;

    public BusinessController(Context context, GraphReader graphReader) {
        this.context = context;
        this.graphReader = graphReader;
    }



    @Override
    public Graph getGraphFromDatabase() {
        return null;
    }

    @Override
    public void selectAlgorithm(AlgorithmType type) {
        this.context.setPathAlgorithm(type);
    }

    @Override
    public AlgorithmResult executeAlgorithm() {
        return this.context.executeAlgorithm();
    }
}
