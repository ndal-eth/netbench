package ch.ethz.systems.netbench.xpt.voijslav.tcp.pfabric;


import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.TransportLayerGenerator;

public class PfabricTransportLayerGenerator extends TransportLayerGenerator {

    public PfabricTransportLayerGenerator() {
        // No parameters needed
        SimulationLogger.logInfo("Transport layer", "PFABRIC");
    }

    @Override
    public TransportLayer generate(int identifier) {
        return new PfabricTransportLayer(
        	identifier,
            Simulator.getConfiguration().getLongPropertyOrFail("seed")
        );
    }

}
