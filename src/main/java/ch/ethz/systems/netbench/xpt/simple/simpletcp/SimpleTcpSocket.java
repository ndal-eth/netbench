package ch.ethz.systems.netbench.xpt.simple.simpletcp;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.Socket;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.simple.TcpPacketResendEvent;
import ch.ethz.systems.netbench.xpt.tcpbase.AckRange;
import ch.ethz.systems.netbench.xpt.tcpbase.AckRangeSet;
import ch.ethz.systems.netbench.xpt.tcpbase.FullExtTcpPacket;
import ch.ethz.systems.netbench.xpt.tcpbase.TcpLogger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static ch.ethz.systems.netbench.xpt.simple.simpletcp.SimpleTcpSocket.State.*;

/**
 * TODO
 *
 */
public class SimpleTcpSocket extends Socket {

    private TcpLogger tcpLogger;

    ////////////////////////////////////////////////////////
    /// TCP CONSTANTS

    // First sequence number
    private static final long FIRST_SEQ_NUMBER = 0;

    // Maximum flow size allowed in bytes (1 terabyte)
    private static final long MAXIMUM_FLOW_SIZE = 1000000000000L;

    // Possible TCP states supported
    enum State {
        LISTEN,         // Awaiting a sender to connect to it
        SYN_SENT,       // Sent out the initial SYN message in the 3-way handshake, awaiting SYN+ACK response
        SYN_RECEIVED,   // Sent out the SYN+ACK message in the 3-way handshake, awaiting ACK response
        ESTABLISHED     // Data transmission
    }


    ////////////////////////////////////////////////////////
    /// TCP PARAMETERS

    // Maximum segment size (MSS) (data carried by a TCP packet); can be dynamically found using MTU-discovery
    private final long MAX_SEGMENT_SIZE;
    private final double MAX_SEGMENT_SIZE_SQUARED;

    // Maximum (congestion) window size
    private final long MAX_WINDOW_SIZE;

    // Loss window
    protected final long LOSS_WINDOW_SIZE;

    // Minimum time-out
    private final long MINIMUM_ROUND_TRIP_TIMEOUT;


    ////////////////////////////////////////////////////////
    /// TCP STATE

    private State currentState;          // state:       Current TCP state
    protected double slowStartThreshold; // ssthresh:    Threshold for congestion window when it goes to congestion avoidance phase
    protected double congestionWindow;   // cwnd:        Congestion window size, maximum size defined by network
    private long sendUnackNumber;        // SND.UNA:     First number which is in window, still unacknowledged
    private long sendNextNumber;         // SND.NXT:     Next sequence number to be used
    private long receiveNextNumber;      // RCV.NXT:     Next sequence number that needs to be received to move window
    private long highestSentOutNumber;   // SND.MAXNXT:  The highest byte sequence number sent out (exclusive)

    // Selective acknowledgment variables
    private AckRangeSet selectiveAckSet;
    private Set<Long> acknowledgedSegStartSeqNumbers;
    private Set<Long> sentOutUnacknowledgedSegStartSeqNumbers;

    // Retransmission time-out variables
    private Map<Long, TcpPacketResendEvent> seqNumbToResendEventMap;
    private boolean firstRttMeasurement;
    private double smoothRoundTripTime;
    private double roundTripTimeVariation;
    private long roundTripTimeout;

    // Flowlet recording
    private long currentFlowlet;

    /**
     * Create a TCP socket. By default, it is the receiver.
     * Use the {@link #start() start} method to make the socket a
     * sender and initiate the communication handshake.
     *
     * @param transportLayer    Transport layer
     * @param flowId            Flow identifier
     * @param sourceId          Source network device identifier
     * @param destinationId     Target network device identifier
     * @param flowSizeByte      Size of the flow in bytes
     */
    public SimpleTcpSocket(TransportLayer transportLayer, long flowId, int sourceId, int destinationId, long flowSizeByte) {
        super(transportLayer, flowId, sourceId, destinationId, flowSizeByte);

        // Load in TCP constant parameters

        // The default round-trip time-out is set rather large:
        //
        // Let's say it takes a 12-hop path (incl. ACK) with two bottlenecks on route
        // Each bottleneck takes: 1500 * 100 * 8 / 10 = 120000ns to cross
        // Each hop takes: 20ns (delay) + 1200ns = 1220ns
        //
        // So: 2*120000 + 12 * 1220 = 254640ns ~= 300000ns = 300 microseconds
        this.roundTripTimeout = Simulator.getConfiguration().getLongPropertyWithDefault("TCP_ROUND_TRIP_TIMEOUT_NS", 1000000L);
        this.MINIMUM_ROUND_TRIP_TIMEOUT = Simulator.getConfiguration().getLongPropertyWithDefault("TCP_MINIMUM_ROUND_TRIP_TIMEOUT", 1000000L);

        // Ethernet: 1500 - 60 (TCP header) - 60 (IP header) = 1380 bytes
        this.MAX_SEGMENT_SIZE = Simulator.getConfiguration().getLongPropertyWithDefault("TCP_MAX_SEGMENT_SIZE", 1380L);

        // Conservative slow start threshold of 30 segments, which is 33120 bytes
        long INITIAL_SLOW_START_THRESHOLD = Simulator.getConfiguration().getLongPropertyWithDefault("TCP_INITIAL_SLOW_START_THRESHOLD", 30 * MAX_SEGMENT_SIZE);

        // Maximum window size is 2^16-1 bytes, which is what is the maximum allowed in the TCP header
        this.MAX_WINDOW_SIZE = Simulator.getConfiguration().getLongPropertyWithDefault("TCP_MAX_WINDOW_SIZE", 1073741824L);

        // Loss window is one segment (by default)
        this.LOSS_WINDOW_SIZE = Simulator.getConfiguration().getLongPropertyWithDefault("TCP_LOSS_WINDOW_SIZE", MAX_SEGMENT_SIZE);

        // Pre-calculate for congestion-avoidance phase
        this.MAX_SEGMENT_SIZE_SQUARED = this.MAX_SEGMENT_SIZE * this.MAX_SEGMENT_SIZE;

        // Per RFC 5681:
        //
        // If (SMSS > 1095 bytes) and (SMSS <= 2190 bytes):
        //   IW = 3 * SMSS bytes and MUST NOT be more than 3 segments
        //
        long INITIAL_WINDOW_SIZE = Simulator.getConfiguration().getLongPropertyWithDefault("TCP_INITIAL_WINDOW_SIZE", 3 * MAX_SEGMENT_SIZE);

        // Illegal maximum segment size vs. window size
        if (this.MAX_SEGMENT_SIZE > INITIAL_WINDOW_SIZE) {
            throw new IllegalArgumentException("Initial/minimum window size (" + INITIAL_WINDOW_SIZE + ") is smaller than the maximum segment size (" + MAX_SEGMENT_SIZE + ").");
        }

        // Illegal window size
        if (INITIAL_WINDOW_SIZE > this.MAX_WINDOW_SIZE) {
            throw new IllegalArgumentException("Initial/minimum window size (" + INITIAL_WINDOW_SIZE + ") is larger than maximum window size (" + MAX_WINDOW_SIZE + ").");
        }

        // Too large flow size
        if (flowSizeByte > MAXIMUM_FLOW_SIZE) {
            throw new IllegalArgumentException("The maximum flow size is 1TB, it is not allowed to exceed this maximum by starting a flow of size " + flowSizeByte + " bytes.");
        }

        // Initialize state
        this.currentState = LISTEN;
        this.sendNextNumber = FIRST_SEQ_NUMBER;
        this.sendUnackNumber = sendNextNumber; // Nothing is sent out
        this.receiveNextNumber = -1; // To be determined by other party
        this.congestionWindow = INITIAL_WINDOW_SIZE;
        this.slowStartThreshold = INITIAL_SLOW_START_THRESHOLD;

        // Time-out event
        seqNumbToResendEventMap = new HashMap<>();
        this.firstRttMeasurement = true;
        this.roundTripTimeVariation = 0;
        this.smoothRoundTripTime = 0;

        // Selective acknowledgments saved
        this.selectiveAckSet = new AckRangeSet();
        this.acknowledgedSegStartSeqNumbers = new HashSet<>();
        this.sentOutUnacknowledgedSegStartSeqNumbers = new HashSet<>();

        // Flowlet tracking
        currentFlowlet = 0;

        // TCP logger
        this.tcpLogger = new TcpLogger(flowId, flowSizeByte == -1);

    }

    @Override
    public void start() {

        // Increment send next number
        long originalSendNextNumber = sendNextNumber;
        sendNextNumber += 1L;

        // Send the first part of the handshake
        currentState = SYN_SENT;
        sendWithResend(createPacket(
                0, // Data size (byte)
                originalSendNextNumber, // Seq number
                0, // Ack number
                false, // ACK
                true,  // SYN
                false  // ECE
        ));

        // System.out.println("3-WAY HANDSHAKE: 0. Sender sent SYN.");

        // Preserve space as this field will never be used
        this.selectiveAckSet = null;

    }

    @Override
    public void handle(Packet genericPacket) {
        FullExtTcpPacket packet = (FullExtTcpPacket) genericPacket;

        // Logging of packets
        if (packet.isACK() && !packet.isSYN()) {
            SimulationLogger.increaseStatisticCounter("TCP_ACK_PACKETS_RECEIVED");
        } else {
            SimulationLogger.increaseStatisticCounter("TCP_DATA_PACKETS_RECEIVED");
        }

        switch (currentState) {

            case ESTABLISHED: {
                establishedHandle(packet);
                break;
            }

            /*
             * LISTEN state.
             *
             * The TCP client was created because of an incoming flow
             * of information. It receives the synchronization (SYN) message
             * as the first part of the three-way handshake. It responds
             * with a SYN+ACK message as the second part.
             */
            case LISTEN: {
                assert(this.isReceiver());

                // If it is SYN, we must be the receiver, else this state does not happen
                if (packet.isSYN() && !packet.isACK()) {

                    // Retrieve the sequence number of the other
                    receiveNextNumber = packet.getSequenceNumber();

                    // We received one byte of information (SYN)
                    receiveNextNumber += 1L;

                    // We sent out 1 byte of information (SYN)
                    // We do this before, so that if we get instantaneous response, it is still valid
                    long originalSendNextNumber = sendNextNumber;
                    sendNextNumber += 1L;

                            // Now that we have received SYN,
                    // we send back a SYN+ACK with our own sequence number
                    currentState = SYN_RECEIVED;
                    sendWithResend(createPacket(
                            0, // Data size (byte)
                            originalSendNextNumber, // Seq number
                            receiveNextNumber, // Ack number
                            true, // ACK
                            true, // SYN
                            false // ECE
                    ));

                    // System.out.println("3-WAY HANDSHAKE: 1. Receiver received SYN, sent back ACK+SYN.");

                }

                break;
            }

            /*
             * SYN_SENT state.
             *
             * The TCP client initiated contact and is awaiting the
             * SYN+ACK from the other side. It responds
             * with a final ACK message as the third part, concluding
             * the three-way handshake.
             */
            case SYN_SENT: {
                assert(!this.isReceiver());

                // If we sent a SYN, we are awaiting a SYN+ACK
                // with the sequence number of the other
                if (packet.isSYN() && packet.isACK()) {

                    // Acknowledgement number (minus 1 for SYN byte) must match
                    // the own outstanding unacknowledged sequence number
                    assert(packet.getAcknowledgementNumber() - 1 == sendUnackNumber);

                    // We no longer need to resend, as we've received acknowledgment
                    cancelResendEvent(sendUnackNumber);

                    // The syn packet we sent is now acknowledged which is 1 "byte" of information (the SYN bit)
                    sendUnackNumber += 1L;

                    // Retrieve sequence number of other
                    receiveNextNumber = packet.getSequenceNumber();

                    // The receiver sent us 1 "byte" of information
                    receiveNextNumber += 1L;

                    // We are now established, send the final acknowledgment to the other party (no SYN bit)
                    currentState = ESTABLISHED;
                    sendWithoutResend(createPacket(
                            0, // Data size (byte)
                            sendNextNumber, // Seq number
                            receiveNextNumber, // Ack number
                            true,  // ACK
                            false, // SYN
                            false // ECE
                    ));

                    // Start sending
                    sendPendingData();

                    // System.out.println("3-WAY HANDSHAKE: 2. Sender received ACK+SYN, sent back ACK.");

                }

                break;
            }

            /*
             * SYN_RECEIVED state.
             *
             * It previously sent out the SYN+ACK in the LISTEN stage,
             * and it awaiting the ACK from the SYN_SENT stage to finalize
             * the three-way handshake.
             */
            case SYN_RECEIVED: {
                assert(this.isReceiver());

                if (packet.isACK() && !packet.isSYN()) {

                    // Ignore any packets except the final ack in the three-way handshake
                    if (packet.getAcknowledgementNumber() != sendNextNumber) {
                        return;
                    }

                    // No longer need to send
                    cancelResendEvent(sendUnackNumber);

                    // Last part of three-way handshake is confirmed
                    sendUnackNumber += 1L;

                    // We don't acknowledge an acknowledgment
                    // so we don't send anything here

                    // Connection is now established
                    currentState = ESTABLISHED;

                    // Set all these initialized maps to zero to preserve memory space
                    this.seqNumbToResendEventMap = null;
                    this.acknowledgedSegStartSeqNumbers = null;
                    this.sentOutUnacknowledgedSegStartSeqNumbers = null;

                    // System.out.println("3-WAY HANDSHAKE: 3. Receiver received ACK.");

                }

                break;
            }

        }

    }

    /**
     * Handling of a packet when in ESTABLISHED state (most interesting).
     *
     * @param packet    TCP packet instance
     */
    private void establishedHandle(FullExtTcpPacket packet) {
        if (packet.isSYN()) {
            // Re-receive the ACK+SYN, means that the receiver has not received the final ACK
            // So we re-send the ACK of the three-way handshake
            handleDataPacket(packet);
        } else if (packet.isACK()) {
            handleAcknowledgment(packet);
        } else {
            handleDataPacket(packet);
        }
    }

    /**
     * In ESTABLISHED state, handle the reception of
     * an acknowledgment packet.
     *
     * @param packet    TCP packet instance
     */
    private void handleDataPacket(TcpPacket packet) {

        // Invariants

        // For the sender, if the ACK message did not arrive, it will receive
        // another ACK+SYN message, which it will then again confirm.
        if (!this.isReceiver()) {
            assert(packet.isSYN() && packet.isACK());
            sendWithoutResend(
                    ((FullExtTcpPacket) createPacket(
                            0, // Data size (byte)
                            sendNextNumber, // Sequence number
                            (packet.getSequenceNumber() + packet.getDataSizeByte() + (packet.isSYN() ? 1 : 0)), // Ack number
                            true, // ACK
                            false, // SYN
                            packet.getECN() // ECE
                    ).setEchoFlowletId(packet.getFlowletId()))
                     .setEchoDepartureTime(packet.getDepartureTime())
            );
            return;
        }

        // The receiver is always at FIRST_SEQ_NUMBER+1, having sent only the ACK+SYN message
        assert(this.sendUnackNumber == FIRST_SEQ_NUMBER + 1);

        // Locally store sequence numbers
        long seqNumber = packet.getSequenceNumber();
        long ackNumber = (packet.getSequenceNumber() + packet.getDataSizeByte() + (packet.isSYN() ? 1 : 0));

        // Only advance if it is the packet on the left side of the receiver window
        if (receiveNextNumber == seqNumber) {
            receiveNextNumber = selectiveAckSet.determineReceiveNextNumber(ackNumber);

        // If it is not on the left side, we selectively acknowledge it
        } else if (seqNumber > receiveNextNumber) {
            selectiveAckSet.add(seqNumber, ackNumber);
        }

        // Send out the acknowledgment
        sendWithoutResend(
                ((FullExtTcpPacket) ((FullExtTcpPacket) (createPacket(
                        0, // Data size (byte)
                        sendNextNumber, // Sequence number
                        receiveNextNumber, // Ack number
                        true, // ACK
                        false, // SYN
                        packet.getECN() // ECE
                ).setEchoFlowletId(packet.getFlowletId())))
                 .setSelectiveAck(selectiveAckSet.createSelectiveAckData()))
                 .setEchoDepartureTime(packet.getDepartureTime())
        );

    }

    /**
     * Handle an acknowledgment packet.
     *
     * @param packet    TCP packet instance
     */
    private void handleAcknowledgment(FullExtTcpPacket packet) {

        // Invariant: receiver can *only* receive a duplicate third handshake acknowledgment
        long ack = packet.getAcknowledgementNumber();
        if (this.isReceiver()) {
            assert(ack == sendNextNumber && sendNextNumber == sendUnackNumber && packet.getDataSizeByte() == 0 && !packet.isSYN());
            return;
        }

        // Flowlet recording
        if (packet.getEchoFlowletId() < currentFlowlet) {
            SimulationLogger.increaseStatisticCounter("TCP_FLOWLET_OUT_OF_ORDER");
        } else {
            currentFlowlet = packet.getEchoFlowletId();
            tcpLogger.logMaxFlowlet(currentFlowlet);
        }

        // If all flow is confirmed, we do not handle any more acknowledgments as sender
        if (isAllFlowConfirmed()) {
            return;
        }

        // Log current congestion window
        tcpLogger.logCongestionWindow(this.congestionWindow);

        // Round-trip time estimation; follows RFC 6298
        double RAcc = (Simulator.getCurrentTime() - packet.getEchoDepartureTime());
        if (firstRttMeasurement) {
            smoothRoundTripTime = RAcc;
            roundTripTimeVariation = smoothRoundTripTime / 2;
            firstRttMeasurement = false;
        } else {
            roundTripTimeVariation = 0.75 * roundTripTimeVariation + 0.25 * Math.abs(smoothRoundTripTime - RAcc);
            smoothRoundTripTime = 0.875 * smoothRoundTripTime + 0.125 * RAcc;
        }
        roundTripTimeout = (long) Math.max(MINIMUM_ROUND_TRIP_TIMEOUT, (2 * smoothRoundTripTime + 4 * roundTripTimeVariation));

        int newPacketsAcked = 0;

        // FLOW ACKNOWLEDGMENT: SELECTIVE ACKNOWLEDGMENT RANGES
        Set<Long> outstandingSegmentSeqs = new HashSet<>(sentOutUnacknowledgedSegStartSeqNumbers);
        for (Long segSeq : outstandingSegmentSeqs) {
            long size = getFlowSizeByte(segSeq);
            long segAck = segSeq + size;

            // Check if an outstanding segment falls within the selective acknowledgment ranges
            for (AckRange r : packet.getSelectiveAck()) {
                if (r.isWithin(segSeq, segAck)) {
                    confirmSegment(segSeq);
                    newPacketsAcked++;
                }
            }

        }

        // FLOW ACKNOWLEDGMENT: CUMULATIVE ACKNOWLEDGMENT
        outstandingSegmentSeqs = new HashSet<>(sentOutUnacknowledgedSegStartSeqNumbers); // Set<Long>
        for (Long segSeq : outstandingSegmentSeqs) {
            long size = getFlowSizeByte(segSeq);
            long segAck = segSeq + size;
            if (segAck <= ack) {
                confirmSegment(segSeq);
                newPacketsAcked++;
            }
        }

        // MOVE WINDOW AS FAR AS POSSIBLE
        // Continue on with the other packets that have also have been
        // received already
        long acknowledgedBytes = 0;
        while (acknowledgedSegStartSeqNumbers.contains(sendUnackNumber)) {
            acknowledgedSegStartSeqNumbers.remove(sendUnackNumber);

            // Retrieve size of the already out-of-order (selectively) acknowledged packet
            long size = getFlowSizeByte(sendUnackNumber);

            // A packet's data is thus acknowledged
            this.confirmFlow(size);

            // Consume the window further
            sendUnackNumber += size;
            acknowledgedBytes += size;

        }

        // The window that we want to send can't start at already acknowledged numbers
        sendNextNumber = Math.max(sendNextNumber, sendUnackNumber);

        // Handle the action if it is marked with ECE
        if (packet.isECE()) {
            handleECEMarkedPacket();
        }

        // Increment window for every packet acknowledged by this acknowledgement
        for (int i = 0; i < newPacketsAcked; i++) {
            phaseIncrementCongestionWindow();
        }

        // Update alpha
        updateAlpha(packet, acknowledgedBytes);

        // Send out as much as possible
        sendPendingData();

        // Log current congestion window
        tcpLogger.logCongestionWindow(this.congestionWindow);

        // Flow is finished if nothing is sent and everything
        // has been acknowledged
        if (sendUnackNumber == sendNextNumber) {
            assert(isAllFlowConfirmed());
            assert(sentOutUnacknowledgedSegStartSeqNumbers == null || sentOutUnacknowledgedSegStartSeqNumbers.isEmpty());
            assert(acknowledgedSegStartSeqNumbers == null || acknowledgedSegStartSeqNumbers.isEmpty());
            assert(seqNumbToResendEventMap == null || seqNumbToResendEventMap.isEmpty());
            this.sentOutUnacknowledgedSegStartSeqNumbers = null;
            this.acknowledgedSegStartSeqNumbers = null;
            this.seqNumbToResendEventMap = null;
            this.selectiveAckSet = null;
        }

    }

    /**
     * Update alpha hook for DCTCP.
     *
     * @param packet                        Packet instance
     * @param dataSizeAcknowledgedBytes     Amount of data actually acknowledged
     */
    protected void updateAlpha(TcpPacket packet, long dataSizeAcknowledgedBytes) {
        // Does nothing.
    }

    /**
     * Handle that an acknowledgment packet was marked with the ECE flag,
     * indicating that on the way there the packet had encountered congestion.
     */
    protected void handleECEMarkedPacket() {
        //halveCongestionWindow();
    }

    /**
     * Fill the congestion window by sending out as much
     * packets as is permitted given the existing window
     * and the packets already sent out but not yet confirmed.
     */
    private void sendPendingData() {
        assert(sendNextNumber >= sendUnackNumber);

        // Calculate congestion window difference
        long lastUnackNumber = sendUnackNumber + (long) Math.min(congestionWindow, MAX_WINDOW_SIZE);
        long difference = lastUnackNumber - sendNextNumber; // Available window

        // Send packets until either the congestion window is full
        // or there is no longer any flow to send
        long amountToSendByte = getFlowSizeByte(sendNextNumber);
        while (difference >= amountToSendByte && amountToSendByte > 0) {

            // If it has not yet been confirmed,actually send out the packet
            if (!acknowledgedSegStartSeqNumbers.contains(sendNextNumber)) {
                sendOutDataPacket(sendNextNumber, amountToSendByte);

            // If it has already been confirmed by selective acknowledgments, just move along
            } else {
                sendNextNumber += amountToSendByte;
            }

            // Determine next amount to send
            difference -= amountToSendByte;
            amountToSendByte = getFlowSizeByte(sendNextNumber);

        }

    }

    /**
     * Send out a data packet with the particular sequence number.
     *
     * @param seq               Sequence number
     * @param amountToSendByte  Amount of data to send out
     */
    private void sendOutDataPacket(long seq, long amountToSendByte) {
        assert(seq <= sendNextNumber && !acknowledgedSegStartSeqNumbers.contains(seq));

        // Log that it is now sent out (could happen again)
        sentOutUnacknowledgedSegStartSeqNumbers.add(seq);

        // Update send next number if applicable
        if (seq == sendNextNumber) {
            sendNextNumber += amountToSendByte;
        }

        // Update the highest sent out number (used to determine flight size)
        highestSentOutNumber = Math.max(highestSentOutNumber, sendNextNumber);

        // Send with wanting a confirmation
        sendWithResend(createPacket(
                amountToSendByte, // Data size (byte)
                seq, // Sequence number
                0, // Ack number
                false, // ACK
                false, // SYN
                false  // ECE
        ));

    }

    // Very handy debug function

    /*private void printf(String s) {
        if (flowId == 0) {
            System.out.println(s);
        }
    }*/

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
            this.congestionWindow += (MAX_SEGMENT_SIZE_SQUARED / 2 / this.congestionWindow);

        }

    }

    /**
     * Confirm the successful confirmation of a segment by a certain packet.
     *
     * @param seq   Sequence number of segment acknowledged
     */
    private void confirmSegment(long seq) {

        // It is no longer outstanding
        sentOutUnacknowledgedSegStartSeqNumbers.remove(seq);

        // Add it to the progression
        acknowledgedSegStartSeqNumbers.add(seq);

        // Cancel its resend event
        cancelResendEvent(seq);

    }

    /**
     * Resend the given packet.
     *
     * @param tcpPacket     TCP packet instance
     */
    public void resend(TcpPacket tcpPacket) {

        // Create new packet to resend
        TcpPacket resentPacket = createPacket(
                tcpPacket.getDataSizeByte(),
                tcpPacket.getSequenceNumber(),
                tcpPacket.getAcknowledgementNumber(),
                tcpPacket.isACK(),
                tcpPacket.isSYN(),
                tcpPacket.isECE()
        );

        // Log statistic
        SimulationLogger.increaseStatisticCounter("TCP_RESEND_OCCURRED");

        // Halve congestion window as the congestion control action
        halveCongestionWindow();

        // Send packet
        this.sendWithResend(resentPacket);

    }

    /**
     * Default congestion control action:
     * sets the congestion window to half its size and sets
     * the slow start threshold to the congestion window.
     */
    private void halveCongestionWindow() {
        this.congestionWindow = Math.min(Math.max(LOSS_WINDOW_SIZE, this.congestionWindow / 2.0), MAX_WINDOW_SIZE);
        this.slowStartThreshold = this.congestionWindow;
    }

    /**
     * Create a TCP packet. Used internally as some flags are not needed to specify every time.
     *
     * @param dataSizeByte      Data size carried in bytes
     * @param sequenceNumber    Sequence number
     * @param ackNumber         Acknowledgment number
     * @param ACK               True iff carries acknowledgment
     * @param SYN               True iff is a synchronization packet
     * @param ECE               True iff it wants to let other party know there congestion was encountered
     *
     * @return  TCP packet instance
     */
    private FullExtTcpPacket createPacket(
            long dataSizeByte,
            long sequenceNumber,
            long ackNumber,
            boolean ACK,
            boolean SYN,
            boolean ECE
    ) {
        return new FullExtTcpPacket(
                flowId, dataSizeByte, sourceId, destinationId,
                100, 80, 80, // TTL, source port, destination port
                sequenceNumber, ackNumber, // Seq number, Ack number
                false, false, ECE, // NS, CWR, ECE
                false, ACK, false, // URG, ACK, PSH
                false, SYN, false, // RST, SYN, FIN
                congestionWindow, 0 // Window size, Priority
        );
    }

    /**
     * Determine the flow size desired for the given sequence number.
     *
     * @param seq   Sequence number
     *
     * @return Flow size in bytes
     */
    private long getFlowSizeByte(long seq) {
        return Math.min(MAX_SEGMENT_SIZE, flowSizeByte - seq + 1);
    }

    /**
     * Sends the packet out, but does not register
     * a resend event. This should only be done for
     * packages which do not need to be acknowledged,
     * namely acknowledgment packets.
     *
     * @param packet     TCP packet instance
     */
    private void sendWithoutResend(Packet packet) {
        SimulationLogger.increaseStatisticCounter("TCP_ACK_PACKETS_SENT");
        transportLayer.send(packet);
    }

    /**
     * Guarantees the sending of the TCP packet by
     * having a continuous resend event happening.
     *
     * @param tcpPacket     TCP packet instance
     */
    private void sendWithResend(TcpPacket tcpPacket) {
        SimulationLogger.increaseStatisticCounter("TCP_DATA_PACKETS_SENT");
        registerResendEvent(tcpPacket);
        transportLayer.send(tcpPacket);
    }

    /**
     * Register the resend event of a packet after the currently determined retransmission time-out.
     *
     * @param tcpPacket     TCP packet instance
     */
    private void registerResendEvent(TcpPacket tcpPacket) {
        TcpPacketResendEvent event = new TcpPacketResendEvent(roundTripTimeout, tcpPacket, this);
        Simulator.registerEvent(event);
        seqNumbToResendEventMap.put(tcpPacket.getSequenceNumber(), event);
    }

    /**
     * Cancel the resend event because the packet has been acknowledged
     * by the receiver. Acknowledgement can be done either via selective
     * acknowledgement (SACK) or cumulative acknowledgement via the ACK field.
     *
     * @param seq    Sequence number of the packet
     */
    private void cancelResendEvent(long seq) {
        TcpPacketResendEvent event = seqNumbToResendEventMap.get(seq);
        event.cancel();
        seqNumbToResendEventMap.remove(seq);
    }

}