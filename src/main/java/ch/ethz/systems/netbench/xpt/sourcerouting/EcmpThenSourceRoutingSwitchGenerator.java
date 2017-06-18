package ch.ethz.systems.netbench.xpt.sourcerouting;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.IntermediaryGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.NetworkDeviceGenerator;

public class EcmpThenSourceRoutingSwitchGenerator extends NetworkDeviceGenerator {

    private final int numNodes;
    private final IntermediaryGenerator intermediaryGenerator;
    private final long switchThresholdBytes;

    public EcmpThenSourceRoutingSwitchGenerator(IntermediaryGenerator intermediaryGenerator, int numNodes) {

        // Standard fields
        this.numNodes = numNodes;
        this.intermediaryGenerator = intermediaryGenerator;
        this.switchThresholdBytes = Simulator.getConfiguration().getIntegerPropertyOrFail("routing_ecmp_then_source_routing_switch_threshold_bytes");

        // Log creation
        SimulationLogger.logInfo("Network device", "ECMP_THEN_SOURCE_ROUTING_SWITCH(numNodes=" + numNodes + ", threshold=" + switchThresholdBytes + ")");

    }

    @Override
    public NetworkDevice generate(int identifier) {
        return this.generate(identifier, null);
    }

    @Override
    public NetworkDevice generate(int identifier, TransportLayer transportLayer) {
        return new EcmpThenSourceRoutingSwitch(identifier, transportLayer, numNodes, intermediaryGenerator.generate(identifier), switchThresholdBytes);
    }

}
