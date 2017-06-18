package ch.ethz.systems.netbench.ext.bare;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.Socket;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.xpt.tcpbase.TcpLogger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BareSocket extends Socket {

    // Constants
    private static final long MAX_SEGMENT_SIZE = 1380L;
    private static final double MAX_SEGMENT_SIZE_SQUARED = MAX_SEGMENT_SIZE * MAX_SEGMENT_SIZE;
    private static final long MIN_WINDOW_SIZE = 3 * 1380;
    private static final double DCTCP_WEIGHT_NEW_ESTIMATION = 0.0625 ; // 0 < g < 1 is the weight given to new samples
    private static final double DCTCP_WEIGHT_OLD_ESTIMATION = 1 - DCTCP_WEIGHT_NEW_ESTIMATION; // against the past estimation of alpha
    private static final long ROUND_TRIP_TIMEOUT_NS = 5000000L; // 5ms

    // TCP state variables
    private double slowStartThreshold;   // ssthresh:    Threshold for congestion window when it goes to congestion avoidance phase
    private double congestionWindow;     // cwnd:        Congestion window size, maximum out size defined by network
    private long sendUnackNumber;        // SND.UNA:     First number which is in window, still unacknowledged
    private long sendNextNumber;         // SND.NXT:     Next sequence number to be used
    private long highestSentOutNumber;   // SND.MAX:     Maximum sent out byte number

    // Retransmission time-out
    private Map<Long, BarePacketResendEvent> seqNumbToResendEventMap;

    // Acknowledgment tracking
    private Set<Long> acknowledgedSegStartSeqNumbers;

    // DCTCP state variables
    private long dctcpTotalBytes = 0;
    private long dctcpMarkedBytes = 0;
    private long dctcpAlphaUpdateWindow = 0;
    private double dctcpAlphaFraction = 0.0;

    private TcpLogger logger;

    /**
     * Create a socket. By default, it should be the receiver.
     * Use the {@link #start() start} method to make the socket a
     * sender and initiate the communication handshake.
     *
     * @param transportLayer Transport layer
     * @param flowId         Flow identifier
     * @param sourceId       Source network device identifier
     * @param destinationId  Target network device identifier
     * @param flowSizeByte   Size of the flow in bytes
     */
    BareSocket(TransportLayer transportLayer, long flowId, int sourceId, int destinationId, long flowSizeByte) {
        super(transportLayer, flowId, sourceId, destinationId, flowSizeByte);
        this.slowStartThreshold = 30000;
        this.congestionWindow = 3 * 1380;
        this.sendNextNumber = 0;
        this.sendUnackNumber = 0;
        this.highestSentOutNumber = 0;
        this.seqNumbToResendEventMap = new HashMap<>();
        this.acknowledgedSegStartSeqNumbers = new HashSet<>();
        this.logger = new TcpLogger(flowId, flowSizeByte == -1);
    }

    @Override
    public void start() {
        sendOutPendingData();
    }

    @Override
    public void handle(Packet genericPacket) {
        BarePacket packet = (BarePacket) genericPacket;

        // As receiver, send an ACK packet back to acknowledge reception
        if (this.isReceiver()) {
            assert(!packet.isACK());

            // Send acknowledgment for every packet (does not abide that the
            // acknowledgment field is normally cumulative)
            sendWithoutResend(new BarePacket(
                    flowId,
                    0,
                    sourceId,
                    destinationId,
                    0,
                    packet.getSequenceNumber() + packet.getDataSizeByte(),
                    packet.getECN(),
                    true,
                    0
            ));

        // As sender, save that flow is confirmed, and send another packet out
        } else {
            assert(packet.isACK());

            // Duplicate acknowledgment
            if (packet.getAcknowledgementNumber() <= sendUnackNumber) {
                throw new RuntimeException("Duplicate acknowledgment; should only happen when resending something.");
                //return;
            }

            // Fetch the original sequence number
            long ackedFlow = getFlowSizeByteByAck(packet.getAcknowledgementNumber());
            long originalSeqNumber = packet.getAcknowledgementNumber() - ackedFlow;

            // Add to acknowledged sequence numbers
            acknowledgedSegStartSeqNumbers.add(originalSeqNumber);

            // Cancel and remove resend event
            BarePacketResendEvent event = seqNumbToResendEventMap.get(originalSeqNumber);
            event.cancel();
            seqNumbToResendEventMap.remove(originalSeqNumber);

            // Move send window as much as possible, including packets
            // that have been received already
            while (acknowledgedSegStartSeqNumbers.contains(sendUnackNumber)) {
                acknowledgedSegStartSeqNumbers.remove(sendUnackNumber);

                // Retrieve size of the already out-of-order (selectively) acknowledged packet
                long size = getFlowSizeByteBySeq(sendUnackNumber);

                // A packet's data is thus acknowledged
                this.confirmFlow(size);

                // Consume the window further
                sendUnackNumber += size;

            }

            // Update the DCTCP alpha fraction
            updateAlpha(packet.isECE(), ackedFlow);

            // Increment congestion window
            if (!packet.isECE()) {
                phaseIncrementCongestionWindow();
            }

            // Log current congestion window
            logger.logCongestionWindow(this.congestionWindow);

            // Send out all pending data using the newly allowed window
            sendOutPendingData();

        }

    }

    /* DEBUG FUNCTION:

    private void print(String s) {
        if (flowId == 2) {
            System.out.println(s);
        }
    }

    */

    /**
     * Send out the pending data.
     */
    private void sendOutPendingData() {

        // Calculate congestion window difference
        long lastUnackNumber = getLastUnackNumber();
        long difference = lastUnackNumber - sendNextNumber; // Available window

        // Send packets until either the congestion window is full
        // or there is no longer any flow to send
        long amountToSendByte = getFlowSizeByteBySeq(sendNextNumber);
        while (difference >= amountToSendByte && amountToSendByte > 0) {

            // Set sequence number
            long seq = sendNextNumber;
            sendNextNumber += amountToSendByte;

            // Update the highest sent out number (used to determine flight size)
            highestSentOutNumber = Math.max(highestSentOutNumber, sendNextNumber);

            // Send with wanting a confirmation
            sendWithResend(new BarePacket(
                    flowId,
                    amountToSendByte,
                    sourceId,
                    destinationId,
                    seq,
                    0,
                    false,
                    false,
                    congestionWindow
            ));

            // Determine next amount to send
            difference -= amountToSendByte;
            amountToSendByte = getFlowSizeByteBySeq(sendNextNumber);

        }

    }

    /**
     * Increase the congestion window exponentially if it is
     * in the slow-start phase, and linearly (roughly one SMSS per RTT)
     * if it is in the congestion-avoidance phase.
     */
    private void phaseIncrementCongestionWindow() {

        // Slow start, scales exponentially: for every packet
        // confirmed, it increases the window with one more
        // outstanding packet and so forth.
        if (this.congestionWindow < slowStartThreshold) {
            this.congestionWindow += MAX_SEGMENT_SIZE;

        // Congestion avoidance, scales linearly
        } else {
            this.congestionWindow += (MAX_SEGMENT_SIZE_SQUARED / 4 / this.congestionWindow);

        }

    }

    /**
     * Guarantees the sending of the packet by
     * planning a resend event which is only
     * canceled when it has been confirmed.
     *
     * @param packet     Packet instance
     */
    private void sendWithResend(BarePacket packet) {
        SimulationLogger.increaseStatisticCounter("RESEND_PACKETS_SENT");

        // Register resend event
        BarePacketResendEvent event = new BarePacketResendEvent(getRoundTripTimeoutNs(), packet, this);
        Simulator.registerEvent(event);

        // Save mapping to enable canceling
        seqNumbToResendEventMap.put(packet.getSequenceNumber(), event);

        // Pass to network
        transportLayer.send(packet);

    }

    /**
     * Sends the packet out, but does not register
     * a resend event. This should only be done for
     * packages which do not need to be acknowledged,
     * namely acknowledgment packets themselves.
     *
     * @param packet     Packet instance
     */
    private void sendWithoutResend(Packet packet) {
        SimulationLogger.increaseStatisticCounter("NO_RESEND_PACKETS_SENT");
        transportLayer.send(packet);
    }

    /**
     * Resend the given packet.
     *
     * @param tcpPacket     TCP packet instance
     */
    void resend(BarePacket tcpPacket) {

        // Create new packet to resend
        BarePacket resentPacket = createPacket(
                tcpPacket.getDataSizeByte(),
                tcpPacket.getSequenceNumber(),
                tcpPacket.getAcknowledgementNumber(),
                tcpPacket.isACK(),
                tcpPacket.isECE()
        );

        // Log statistic
        SimulationLogger.increaseStatisticCounter("RESEND_OCCURRED");

        // Halve congestion window as the congestion control action
        packetLostAction();

        // Send packet
        this.sendWithResend(resentPacket);

    }

    /**
     * Take action when a packet is lost.
     * Typical response is halving the congestion window.
     */
    private void packetLostAction() {
        this.congestionWindow = Math.max(MIN_WINDOW_SIZE, this.congestionWindow / 2);
        this.slowStartThreshold = congestionWindow;
    }

    /**
     * Update the DCTCP alpha fraction and adjust the congestion window proportionally to it.
     *
     * @param isECE                     True iff the packet was marked ECE
     * @param dataSizeAcknowledgedBytes Amount of data acknowledged by the packet
     */
    private void updateAlpha(boolean isECE, long dataSizeAcknowledgedBytes) {

        // ECN statistics
        dctcpTotalBytes += dataSizeAcknowledgedBytes;
        if (isECE) {
            dctcpMarkedBytes += dataSizeAcknowledgedBytes;
        }

        // Check if a full window has already passed
        if (dctcpTotalBytes >= dctcpAlphaUpdateWindow) {

            // Update the fraction: alpha <- (1 - g) * alpha + g * F
            if (dctcpTotalBytes > 0) {
                dctcpAlphaFraction = DCTCP_WEIGHT_OLD_ESTIMATION * dctcpAlphaFraction + DCTCP_WEIGHT_NEW_ESTIMATION * ((double) dctcpMarkedBytes) / ((double) dctcpTotalBytes);
            } else {
                dctcpAlphaFraction = 0.0;
            }

            // Reset counters
            dctcpMarkedBytes = 0;
            dctcpTotalBytes = 0;

            // It must wait until a full currently sized congestion window has been
            // sent until it can re-evaluate the window (this is roughly once per RTT)
            dctcpAlphaUpdateWindow = getFlightSize();

        }

        // Decrease congestion window
        if (isECE) {
            congestionWindow = Math.max(MIN_WINDOW_SIZE, (1 - dctcpAlphaFraction / 2.0) * congestionWindow);
            slowStartThreshold = congestionWindow;
        }

    }

    /**
     * Calculate flight size (how many bytes are currently sent out).
     *
     * @return  Flight size
     */
    private long getFlightSize() {
        return highestSentOutNumber - sendUnackNumber;
    }

    /**
     * Create a packet. Used internally as some flags are not needed to specify every time.
     *
     * @param dataSizeByte      Data size carried in bytes
     * @param sequenceNumber    Sequence number
     * @param ackNumber         Acknowledgment number
     * @param ACK               True iff carries acknowledgment
     * @param ECE               True iff it wants to let other party know there congestion was encountered
     *
     * @return  TCP packet instance
     */
    private BarePacket createPacket(
            long dataSizeByte,
            long sequenceNumber,
            long ackNumber,
            boolean ACK,
            boolean ECE
    ) {
        return new BarePacket(
                flowId,
                dataSizeByte,
                sourceId,
                destinationId,
                sequenceNumber,
                ackNumber,
                ECE,
                ACK,
                congestionWindow
        );
    }

    /**
     * Last possible unacknowledged byte number.
     *
     * @return  Last possible byte number (exclusive) that the congestion window permits
     */
    private long getLastUnackNumber() {
        return sendUnackNumber + (long) congestionWindow;
    }

    /**
     * Determine the packet flow size desired for the given sequence number.
     *
     * @param seq   Sequence number
     *
     * @return Flow size in bytes
     */
    private long getFlowSizeByteBySeq(long seq) {
        return Math.min(MAX_SEGMENT_SIZE, flowSizeByte - seq);
    }

    /**
     * Determine the packet flow size desired for the given acknowledgment number.
     *
     * @param ack   Acknowledgement number
     *
     * @return Flow size in bytes
     */
    private long getFlowSizeByteByAck(long ack) {
        if (ack == flowSizeByte) {
            return flowSizeByte % MAX_SEGMENT_SIZE;
        } else {
            return MAX_SEGMENT_SIZE;
        }
    }

    /**
     * Retrieve the round trip timeout (RTT) in nanoseconds.
     *
     * @return  Currently estimated round trip timeout
     */
    private long getRoundTripTimeoutNs() {
        return ROUND_TRIP_TIMEOUT_NS;
    }

}
