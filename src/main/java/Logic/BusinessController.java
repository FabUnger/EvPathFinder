package Logic;

import Data.AlgorithmResult;

public class BusinessController implements Controller {

    private final Context context;

    public BusinessController(Context context) {
        this.context = context;
    }


    @Override
    public void loadGraphFromDataBase() {

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
