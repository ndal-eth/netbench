package ch.ethz.systems.netbench.xpt.tcpbase;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.log.LogFailureException;
import ch.ethz.systems.netbench.core.log.LoggerCallback;
import ch.ethz.systems.netbench.core.log.SimulationLogger;

import java.io.BufferedWriter;
import java.io.IOException;

public class TcpLogger implements LoggerCallback {

    private final long flowId;
    private long maxFlowlet;
    private final BufferedWriter congestionWindowWriter;
    private final BufferedWriter packetBurstGapWriter;
    private final BufferedWriter maxFlowletWriter;
    private final boolean logPacketBurstGapEnabled;
    private final boolean logCongestionWindowEnabled;
    private final boolean isReceiver;

    public TcpLogger(long flowId, boolean isReceiver) {
        this.flowId = flowId;
        this.maxFlowlet = 0;
        this.congestionWindowWriter = SimulationLogger.getExternalWriter("congestion_window.csv.log");
        this.packetBurstGapWriter = SimulationLogger.getExternalWriter("packet_burst_gap.csv.log");
        this.maxFlowletWriter = SimulationLogger.getExternalWriter("max_flowlet.csv.log");
        this.logPacketBurstGapEnabled = Simulator.getConfiguration().getBooleanPropertyWithDefault("enable_log_packet_burst_gap", false);
        this.logCongestionWindowEnabled = Simulator.getConfiguration().getBooleanPropertyWithDefault("enable_log_congestion_window", false);
        this.isReceiver = isReceiver;
        SimulationLogger.registerCallbackBeforeClose(this);
    }

    /**
     * Log the congestion window of a specific flow at a certain point in time.
     *
     * @param congestionWindow      Current size of congestion window
     */
    public void logCongestionWindow(double congestionWindow) {
        if (logCongestionWindowEnabled) {
            try {
                congestionWindowWriter.write(flowId + "," + congestionWindow + "," + Simulator.getCurrentTime() + "\n");
            } catch (IOException e) {
                throw new LogFailureException(e);
            }
        }
    }

    /**
     * Log the maximum flowlet identifier observed acknowledged.
     *
     * @param flowlet   Flowlet identifier
     */
    public void logMaxFlowlet(long flowlet) {
        assert(flowlet >= maxFlowlet);
        maxFlowlet = flowlet;
    }

    /**
     * Log the packet burst gap (ns).
     *
     * @param gapNs Packet burst gap in nanoseconds
     */
    public void logPacketBurstGap(long gapNs) {
        try {
            if (logPacketBurstGapEnabled) {
                packetBurstGapWriter.write(gapNs + "\n");
            }
        } catch (IOException e) {
            throw new LogFailureException(e);
        }
    }

    @Override
    public void callBeforeClose() {
        try {
            if (!isReceiver) {
                maxFlowletWriter.write(flowId + "," + maxFlowlet + "\n");
            }
        } catch (IOException e) {
            throw new LogFailureException(e);
        }
    }

}
