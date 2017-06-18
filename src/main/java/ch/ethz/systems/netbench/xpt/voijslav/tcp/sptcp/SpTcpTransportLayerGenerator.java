package ch.ethz.systems.netbench.xpt.voijslav.tcp.sptcp;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.TransportLayerGenerator;

public class SpTcpTransportLayerGenerator extends TransportLayerGenerator {

    public SpTcpTransportLayerGenerator() {
        // No parameters needed
        SimulationLogger.logInfo("Transport layer", "SP TCP");
    }

    @Override
    public TransportLayer generate(int identifier) {
        return new SpTcpTransportLayer(identifier);
    }

}
