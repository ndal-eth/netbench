package ch.ethz.systems.netbench.core.log;

import ch.ethz.systems.netbench.core.Simulator;

import static ch.ethz.systems.netbench.core.Simulator.getConfiguration;

public class FlowLogger {

    // Every how many packets does it write the interval
    // of throughput
    private static final long STATISTIC_SAMPLE_INTERVAL_BYTES = 50000;

    // Static flow information
    private final long flowId;
    private final int sourceId;
    private final int targetId;
    private final long flowSizeByte;

    // Statistic tracking variables
    private long totalBytesReceived;
    private long receivedBytes;
    private long flowStartTime;
    private long measureStartTime;
    private long flowEndTime;

    // Logging
    private final boolean flowThroughputEnabled;

    public FlowLogger(long flowId, int sourceId, int targetId, long flowSizeByte) {
        this.flowId = flowId;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.flowSizeByte = flowSizeByte;

        this.flowStartTime = Simulator.getCurrentTime();
        this.measureStartTime = Simulator.getCurrentTime();
        this.totalBytesReceived = 0;
        this.receivedBytes = 0;
        this.flowEndTime = -1;

        // Register logger only for sender
        if (this.flowSizeByte != -1) { // Exclude receiving sockets
            SimulationLogger.registerFlowLogger(this);
        }

        // True iff the flow throughput is enabled (or defaulted)
        flowThroughputEnabled = getConfiguration().getBooleanPropertyWithDefault("enable_log_flow_throughput", true);

    }

    /**
     * Log that the some amount of the flow, <code>sizeByte</code>, has
     * been successfully transmitted and confirmed.
     *
     * @param sizeByte  Size of flow successfully transmitted
     */
    public void logFlowAcknowledged(long sizeByte) {
        receivedBytes += sizeByte;
        totalBytesReceived += sizeByte;
        if (receivedBytes > STATISTIC_SAMPLE_INTERVAL_BYTES || flowSizeByte == totalBytesReceived) {
            if (flowThroughputEnabled) {
                SimulationLogger.logFlowThroughput(flowId, sourceId, targetId, receivedBytes, measureStartTime, Simulator.getCurrentTime());
            }
            receivedBytes = 0;
            measureStartTime = Simulator.getCurrentTime();
            if (flowSizeByte == totalBytesReceived) {
                flowEndTime = Simulator.getCurrentTime();
            }
        }
    }

    /**
     * Retrieve flow identifier.
     *
     * @return  Flow identifier
     */
    long getFlowId() {
        return flowId;
    }

    /**
     * Retrieve source node identifier.
     *
     * @return  Source node identifier
     */
    int getSourceId() {
        return sourceId;
    }

    /**
     * Retrieve target node identifier.
     *
     * @return  Target node identifier
     */
    int getTargetId() {
        return targetId;
    }

    /**
     * Retrieve total amount of bytes received (confirmed).
     *
     * @return  Total amount of bytes received
     */
    long getTotalBytesReceived() {
        return totalBytesReceived;
    }

    /**
     * Register original starting time of the flow.
     *
     * @return  Flow starting time (ns since simulation epoch)
     */
    long getFlowStartTime() {
        return flowStartTime;
    }

    /**
     * Register original end time of the flow.
     *
     * @return  Flow end time (ns since simulation epoch)
     */
    long getFlowEndTime() {
        return flowEndTime;
    }

    /**
     * Retrieve total size of the flow.
     *
     * @return  Flow size
     */
    long getFlowSizeByte() {
        return flowSizeByte;
    }

    /**
     * Check whether the flow is completed (all bytes acknowledged/confirmed).
     *
     * @return  True iff flow has been completed confirmed
     */
    boolean isCompleted() {
        return totalBytesReceived == flowSizeByte;
    }

}
