package ch.ethz.systems.netbench.ext.valiant;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.infrastructure.IntermediaryGenerator;
import ch.ethz.systems.netbench.core.run.infrastructure.NetworkDeviceGenerator;

public class RangeValiantSwitchGenerator extends NetworkDeviceGenerator {

    private final int numNodes;
    private final IntermediaryGenerator intermediaryGenerator;
    private final int nodeRangeLower;
    private final int nodeRangeUpper;

    public RangeValiantSwitchGenerator(IntermediaryGenerator intermediaryGenerator, int numNodes) {
        SimulationLogger.logInfo("Network device", "RANGE_VALIANT_SWITCH(numNodes=" + numNodes + ")");

        // Standard fields
        this.numNodes = numNodes;
        this.intermediaryGenerator = intermediaryGenerator;

        // Range of [lower, upper] for which nodes are eligible to be chosen as valiant node
        this.nodeRangeLower = Simulator.getConfiguration().getIntegerPropertyOrFail("routing_random_valiant_node_range_lower_incl");
        this.nodeRangeUpper = Simulator.getConfiguration().getIntegerPropertyOrFail("routing_random_valiant_node_range_upper_incl");

        // Check range
        if (nodeRangeLower < 0 || nodeRangeUpper < 0 || nodeRangeLower > numNodes - 1 || nodeRangeUpper > numNodes - 1 || nodeRangeLower > nodeRangeUpper) {
            throw new IllegalArgumentException("Invalid valiant node range [" + nodeRangeLower + ", " + nodeRangeUpper + "] for n=" + numNodes);
        }

    }

    @Override
    public NetworkDevice generate(int identifier) {
        return this.generate(identifier, null);
    }

    @Override
    public NetworkDevice generate(int identifier, TransportLayer transportLayer) {
        return new RangeValiantSwitch(identifier, transportLayer, numNodes, intermediaryGenerator.generate(identifier), nodeRangeLower, nodeRangeUpper);
    }

}
