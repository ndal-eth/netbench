package ch.ethz.systems.netbench.ext.flowlet;

import ch.ethz.systems.netbench.core.Simulator;

public abstract class FixedGapFlowletIntermediary extends FlowletIntermediary {

    // Flowlet gap in nanoseconds
    private final long FLOWLET_GAP_NS;

    public FixedGapFlowletIntermediary() {
        super();
        this.FLOWLET_GAP_NS = Simulator.getConfiguration().getLongPropertyOrFail("FLOWLET_GAP_NS");
    }

    /**
     * Check whether the gap between the packet being sent right now and
     * the previous packet sent is larger than the flowlet gap set.
     * Automatically sets the last-sent-time to that of the new packet (now).
     *
     * @param flowId    Flow identifier
     *
     * @return  True iff flowlet gap is exceeded (time_gap > flowlet_gap)
     */
    protected boolean flowletGapExceeded(long flowId) {

        // When was the last packet sent in this flow
        Long lastSent = getAndUpdateLastSent(flowId);

        // Switch flowlet if the time between now and the last packet being
        // sent is greater than the flowlet gap
        long delta = Simulator.getCurrentTime() - lastSent;
        // TODO: SimulationLogger.logPacketBurstGap(delta);
        return delta >= FLOWLET_GAP_NS;

    }

}
