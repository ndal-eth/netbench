package ch.ethz.systems.netbench.ext.bare;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.TransportLayerGenerator;

public class BareTransportLayerGenerator extends TransportLayerGenerator {

    public BareTransportLayerGenerator() {
        // No parameters needed
        SimulationLogger.logInfo("Transport layer", "BARE");
    }

    @Override
    public TransportLayer generate(int identifier) {
        return new BareTransportLayer(identifier);
    }

}
