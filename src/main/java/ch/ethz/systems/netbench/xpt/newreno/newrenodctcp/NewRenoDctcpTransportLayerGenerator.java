package ch.ethz.systems.netbench.xpt.newreno.newrenodctcp;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.TransportLayerGenerator;

public class NewRenoDctcpTransportLayerGenerator extends TransportLayerGenerator {

    public NewRenoDctcpTransportLayerGenerator() {
        // No parameters needed
        SimulationLogger.logInfo("Transport layer", "DCTCP");
    }

    @Override
    public TransportLayer generate(int identifier) {
        return new NewRenoDctcpTransportLayer(identifier);
    }

}
