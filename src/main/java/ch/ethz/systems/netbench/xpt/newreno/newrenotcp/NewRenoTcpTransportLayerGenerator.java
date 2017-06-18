package ch.ethz.systems.netbench.xpt.newreno.newrenotcp;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.TransportLayerGenerator;

public class NewRenoTcpTransportLayerGenerator extends TransportLayerGenerator {

    public NewRenoTcpTransportLayerGenerator() {
        // No parameters needed
        SimulationLogger.logInfo("Transport layer", "TCP");
    }

    @Override
    public TransportLayer generate(int identifier) {
        return new NewRenoTcpTransportLayer(identifier);
    }

}
