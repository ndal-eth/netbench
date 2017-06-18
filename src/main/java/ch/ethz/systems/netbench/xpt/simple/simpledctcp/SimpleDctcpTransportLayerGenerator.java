package ch.ethz.systems.netbench.xpt.simple.simpledctcp;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.TransportLayerGenerator;

public class SimpleDctcpTransportLayerGenerator extends TransportLayerGenerator {

    public SimpleDctcpTransportLayerGenerator() {
        // No parameters needed
        SimulationLogger.logInfo("Transport layer", "SIMPLE_DCTCP");
    }

    @Override
    public TransportLayer generate(int identifier) {
        return new SimpleDctcpTransportLayer(identifier);
    }

}
