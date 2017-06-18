package ch.ethz.systems.netbench.xpt.voijslav.tcp.distrandtcp;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.TransportLayerGenerator;

public class DistRandTcpTransportLayerGenerator extends TransportLayerGenerator {

    public DistRandTcpTransportLayerGenerator() {
        // No parameters needed
        SimulationLogger.logInfo("Transport layer", "DistRandTcp");
    }

    @Override
    public TransportLayer generate(int identifier) {
        return new DistRandTcpTransportLayer(
        	identifier,
            Simulator.getConfiguration().getLongPropertyOrFail("seed")
        );
    }

}