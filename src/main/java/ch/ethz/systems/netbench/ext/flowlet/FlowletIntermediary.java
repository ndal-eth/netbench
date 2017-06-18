package ch.ethz.systems.netbench.ext.flowlet;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.network.Intermediary;

import java.util.HashMap;
import java.util.Map;

public abstract class FlowletIntermediary extends Intermediary {

    // Mapping of flow identifier to its current flowlet
    private final Map<Long, Integer> flowIdToCurrentFlowlet;

    // Mapping of flow identifier to the time of the last sent packet
    private final Map<Long, Long> flowIdToLastSent;

    /**
     * Constructor.
     * Creates the mapping tables to track flow identifiers across time.
     */
    protected FlowletIntermediary() {
        this.flowIdToCurrentFlowlet = new HashMap<>();
        this.flowIdToLastSent = new HashMap<>();
    }

    /**
     * Retrieve the last time since simulation epoch that this
     * specific flow has sent a packet. Immediately updates the
     * last sent to right now, for the next invocation.
     *
     * @param flowId    Flow identifier
     *
     * @return  Last sent moment since simulation epoch
     */
    protected long getAndUpdateLastSent(long flowId) {

        // When was the last packet sent in this flow
        Long lastSent = flowIdToLastSent.get(flowId);
        if (lastSent == null) {
            lastSent = Simulator.getCurrentTime();
        }

        // Register that on this flow another packet has been sent now
        flowIdToLastSent.put(flowId, Simulator.getCurrentTime());

        return lastSent;

    }

    /**
     * Retrieve the current flowlet to which this packet belongs in its flow.
     *
     * @param flowId    Flow identifier
     *
     * @return  Current flowlet (identifier)
     */
    protected int getCurrentFlowlet(long flowId) {
        Integer currentFlowlet = flowIdToCurrentFlowlet.get(flowId);
        if (currentFlowlet == null) {
            currentFlowlet = 0;
        }
        return currentFlowlet;
    }

    /**
     * Update the current flowlet of the flow to which this packet belongs.
     *
     * @param flowId        Flow identifier
     * @param newFlowlet    New flowlet all consecutive packets will have as long as the in-between time
     *                      does not exceed the flowlet gap
     */
    protected void setCurrentFlowlet(long flowId, int newFlowlet) {
        flowIdToCurrentFlowlet.put(flowId, newFlowlet);
    }

}
