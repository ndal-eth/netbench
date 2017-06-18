package ch.ethz.systems.netbench.xpt.simple.simpletcp;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.TransportLayerGenerator;

public class SimpleTcpTransportLayerGenerator extends TransportLayerGenerator {

    public SimpleTcpTransportLayerGenerator() {
        // No parameters needed
        SimulationLogger.logInfo("Transport layer", "TCP");
    }

    @Override
    public TransportLayer generate(int identifier) {
        return new SimpleTcpTransportLayer(identifier);
    }

}
