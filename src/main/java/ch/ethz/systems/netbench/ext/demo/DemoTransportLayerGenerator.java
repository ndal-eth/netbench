package ch.ethz.systems.netbench.ext.demo;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.TransportLayerGenerator;

public class DemoTransportLayerGenerator extends TransportLayerGenerator {

    public DemoTransportLayerGenerator() {
        // No parameters needed
        SimulationLogger.logInfo("Transport layer", "DEMO");
    }

    @Override
    public TransportLayer generate(int identifier) {
        return new DemoTransportLayer(identifier);
    }

}
