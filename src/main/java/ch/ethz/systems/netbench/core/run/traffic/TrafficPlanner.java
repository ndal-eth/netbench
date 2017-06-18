package ch.ethz.systems.netbench.core.run.traffic;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.GraphDetails;
import ch.ethz.systems.netbench.core.network.TransportLayer;

import java.util.Map;

public abstract class TrafficPlanner {

    protected final Map<Integer, TransportLayer> idToTransportLayerMap;
    protected final GraphDetails graphDetails;

    /**
     * Constructor.
     *
     * @param idToTransportLayerMap     Maps a network device identifier to its corresponding transport layer
     */
    public TrafficPlanner(Map<Integer, TransportLayer> idToTransportLayerMap) {

        // Create mappings
        this.idToTransportLayerMap = idToTransportLayerMap;
        this.graphDetails = Simulator.getConfiguration().getGraphDetails();

    }

    public abstract void createPlan(long durationNs);

    /**
     * Register the flow from [srcId] to [dstId].
     *
     * @param time          Time at which it start in nanoseconds
     * @param srcId         Source network device identifier
     * @param dstId         Destination network device identifier
     * @param flowSizeByte  Flow size in bytes
     */
    protected void registerFlow(long time, int srcId, int dstId, long flowSizeByte) {

        // Some checking
        if (srcId == dstId) {
            throw new RuntimeException("Invalid traffic pair; source (" + srcId + ") and destination (" + dstId + ") are the same.");
        } else if (idToTransportLayerMap.get(srcId) == null) {
            throw new RuntimeException("Source network device " + srcId + " does not have a transport layer.");
        } else if (idToTransportLayerMap.get(dstId) == null) {
            throw new RuntimeException("Destination network device " + dstId + ") does not have a transport layer.");
        } else if (time < 0) {
            throw new RuntimeException("Cannot register a flow with a negative timestamp of " + time);
        } else if (flowSizeByte < 0) {
            throw new RuntimeException("Cannot register a flow with a negative flow size (in bytes) of " + flowSizeByte);
        }

        // Create event
        FlowStartEvent event = new FlowStartEvent(time, idToTransportLayerMap.get(srcId), dstId, flowSizeByte);

        // Register event
        Simulator.registerEvent(event);

    }

}
