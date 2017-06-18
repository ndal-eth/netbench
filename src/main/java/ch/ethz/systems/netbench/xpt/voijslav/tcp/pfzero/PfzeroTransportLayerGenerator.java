package ch.ethz.systems.netbench.xpt.voijslav.tcp.pfzero;


import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.TransportLayerGenerator;

public class PfzeroTransportLayerGenerator extends TransportLayerGenerator {

    public PfzeroTransportLayerGenerator() {
        // No parameters needed
        SimulationLogger.logInfo("Transport layer", "PFZERO");
    }

    @Override
    public TransportLayer generate(int identifier) {
        return new PfzeroTransportLayer(
        	identifier,
            Simulator.getConfiguration().getLongPropertyOrFail("seed")
        );
    }

}
