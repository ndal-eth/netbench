package ch.ethz.systems.netbench.xpt.voijslav.tcp.lstftcp;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.TransportLayerGenerator;

public class LstfTcpTransportLayerGenerator extends TransportLayerGenerator {

    public LstfTcpTransportLayerGenerator() {
        // No parameters needed
        SimulationLogger.logInfo("Transport layer", "LSTF TCP");
    }

    @Override
    public TransportLayer generate(int identifier) {
        return new LstfTcpTransportLayer(identifier);
    }

}