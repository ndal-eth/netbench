package ch.ethz.systems.netbench.xpt.voijslav.tcp.sphalftcp;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.TransportLayerGenerator;

public class SpHalfTcpTransportLayerGenerator extends TransportLayerGenerator {

    public SpHalfTcpTransportLayerGenerator() {
        // No parameters needed
        SimulationLogger.logInfo("Transport layer", "SP HALF TCP");
    }

    @Override
    public TransportLayer generate(int identifier) {
        return new SpHalfTcpTransportLayer(identifier);
    }

}