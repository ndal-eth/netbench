package ch.ethz.systems.netbench.xpt.newreno.newrenodctcp;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.xpt.newreno.newrenotcp.NewRenoTcpSocket;

public class NewRenoDctcpSocket extends NewRenoTcpSocket {

    private final double DCTCP_WEIGHT_NEW_ESTIMATION ; // 0 < g < 1 is the weight given to new samples
    private final double DCTCP_WEIGHT_OLD_ESTIMATION; // against the past estimation of alpha

    private double alphaFraction;
    private long totalBytes;
    private long markedBytes;
    private long alphaUpdateSequenceNumber;

    /**
     * Create a New Reno TCP socket. By default, it is the receiver.
     * Use the {@link #start() start} method to make the socket a
     * sender and initiate the communication handshake.
     *
     * @param transportLayer Transport layer
     * @param flowId         Flow identifier
     * @param sourceId       Source network device identifier
     * @param destinationId  Target network device identifier
     * @param flowSizeByte   Size of the flow in bytes
     */
    NewRenoDctcpSocket(TransportLayer transportLayer, long flowId, int sourceId, int destinationId, long flowSizeByte) {
        super(transportLayer, flowId, sourceId, destinationId, flowSizeByte);
        this.DCTCP_WEIGHT_NEW_ESTIMATION = Simulator.getConfiguration().getDoublePropertyWithDefault("DCTCP_WEIGHT_NEW_ESTIMATION", 0.0625);
        this.DCTCP_WEIGHT_OLD_ESTIMATION = 1.0 - DCTCP_WEIGHT_NEW_ESTIMATION;
        this.alphaFraction = 0.0;
        this.totalBytes = 0;
        this.markedBytes = 0;
        this.alphaUpdateSequenceNumber = 0;
    }

    /**
     * Handle a successful acknowledgment.
     * DCTCP maintains a running fraction of packets affected
     * by explicit congestion notification and adjusts the rate
     * on the fly.
     *
     * @param   ack     Acknowledgment number
     * @param   isECE   Whether congestion was encountered
     */
    @Override
    protected void processAcknowledgment(long ack, boolean isECE) {

        // Number of acknowledged bytes
        long ackedBytes = Math.max(0, ack - sendUnackNumber);

        // ECN statistics
        totalBytes += ackedBytes;
        if (isECE) {
            markedBytes += ackedBytes;
        }

        // Check if a full window has already passed
        if (ack >= alphaUpdateSequenceNumber) {

            // Update the fraction: alpha <- (1 - g) * alpha + g * F
            if (totalBytes > 0) {
                alphaFraction = DCTCP_WEIGHT_OLD_ESTIMATION * alphaFraction + DCTCP_WEIGHT_NEW_ESTIMATION * ((double) markedBytes) / ((double) totalBytes);
            } else {
                alphaFraction = 0.0;
            }

            // Reset counters
            markedBytes = 0;
            totalBytes = 0;

            // It must wait until a full currently sized congestion window has been
            // sent until it can re-evaluate the window
            alphaUpdateSequenceNumber = ack + (long) congestionWindow;

        }

        // Reduce congestion window size with the current fraction
        if (isECE) {
            congestionWindow = Math.max(LOSS_WINDOW_SIZE, (1 - alphaFraction / 2.0) * congestionWindow);
            slowStartThreshold = Math.max(MINIMUM_SSTHRESH, congestionWindow);
        }

        // The base TCP handling
        super.processAcknowledgment(ack, isECE);

    }


    @Override
    protected void phaseIncrementCongestionWindow(boolean isECE) {

        // If there is no ECN marker, increment the congestion window, like regular TCP
        if (!isECE) {
            super.phaseIncrementCongestionWindow(false);
        }

    }

    /**
     * Retrieve the current alpha (rate reduction) fraction.
     *
     * @return  Alpha fraction
     */
    public double getCurrentAlphaFraction() {
        return alphaFraction;
    }

}
