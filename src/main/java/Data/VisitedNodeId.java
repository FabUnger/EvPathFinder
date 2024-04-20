package Data;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;

public class VisitedNodeId {

    private static final AtomicInteger counter = new AtomicInteger(new Random().nextInt(1000));

    private final String name;
    private final int version;

    public VisitedNodeId(String id) {
        this.name = id;
        this.version = counter.incrementAndGet();
    }

    public String getName() {
        return this.name;
    }

    public int getVersion() {
        return this.version;
    }
}
