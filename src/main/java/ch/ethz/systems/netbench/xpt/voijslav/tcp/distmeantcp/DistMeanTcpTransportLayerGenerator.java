package ch.ethz.systems.netbench.xpt.voijslav.tcp.distmeantcp;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.TransportLayerGenerator;

public class DistMeanTcpTransportLayerGenerator extends TransportLayerGenerator {

    public DistMeanTcpTransportLayerGenerator() {
        // No parameters needed
        SimulationLogger.logInfo("Transport layer", "DistMeanTcp");
    }

    @Override
    public TransportLayer generate(int identifier) {
        return new DistMeanTcpTransportLayer(
        	identifier,
            Simulator.getConfiguration().getLongPropertyOrFail("seed")
        );
    }

}