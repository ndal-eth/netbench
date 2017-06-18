package ch.ethz.systems.netbench.ext.poissontraffic.flowsize;

import ch.ethz.systems.netbench.core.Simulator;

import java.util.Random;

public abstract class FlowSizeDistribution {

    Random independentRng;

    FlowSizeDistribution() {
        this.independentRng = Simulator.selectIndependentRandom("flow_size");
    }

    public abstract long generateFlowSizeByte();
}
