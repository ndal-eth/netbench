package ch.ethz.systems.netbench.xpt.newreno.newrenotcp;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.Socket;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.newreno.TcpRetransmissionTimeOutEvent;
import ch.ethz.systems.netbench.xpt.tcpbase.AckRange;
import ch.ethz.systems.netbench.xpt.tcpbase.AckRangeSet;
import ch.ethz.systems.netbench.xpt.tcpbase.FullExtTcpPacket;
import ch.ethz.systems.netbench.xpt.tcpbase.TcpLogger;

import java.util.HashSet;
import java.util.Set;

import static ch.ethz.systems.netbench.xpt.newreno.newrenotcp.NewRenoTcpSocket.State.*;

/**
 * Imitates a simplified version of the TCP New Reno protocol,
 * while still preserving the effect of its congestion
 * handling features.
 *
 * Currently includes:
 * - Three-way handshake + established (per RFC 793 under the assumption of no crash)
 * - Time-out resend (per RFC 6298)
 * - Slow-start and congestion-avoidance phase (per RFC 5681)
 * - Selective acknowledgments (per RFC 2018 without any limitation on number of options)
 * - Fast re-transmit and fast recovery (per RFC 6582)
 * - Nagle's algorithm (simplified, as there is a continuous availability of new data)
 * - Limited transmit algorithm (per RFC 3042)
 * - Dynamic RTO determination (per RFC 6298)
 *
 * Excludes on purpose:
 * - Crash simulation (e.g. other TCP states)
 * - Maximum window size for receiver
 * - Loop-around sequence numbers
 *
 * Could include, but does not currently:
 * - Delayed acknowledgments
 *
 * Limitations:
 * - Maximum flow size of 1 terabyte
 *
 * Crucial RFCs:
 * - RFC 793:  https://tools.ietf.org/html/rfc793   :  Transmission Control Protocol
 * - RFC 2018: https://tools.ietf.org/html/rfc2018  :  TCP Selective Acknowledgment Options
 * - RFC 3042: https://tools.ietf.org/html/rfc3042  :  Enhancing TCP's Loss Recovery Using Limited Transmit
 * - RFC 5681: https://tools.ietf.org/html/rfc5681  :  TCP Congestion Control (slow start, congestion avoidance, fast retransmit, and fast recovery)
 * - RFC 6298: https://tools.ietf.org/html/rfc6298  :  Computing TCP's Retransmission Timer
 * - RFC 6582: https://tools.ietf.org/html/rfc6582  :  The NewReno Modification to TCP's Fast Recovery Algorithm
 * - RFC 7414: https://tools.ietf.org/html/rfc7414  :  A Roadmap for Transmission Control Protocol (TCP) Specification Documents
 *
 * Makes use of ns-3 source code for TCP New Reno:
 * https://www.nsnam.org/docs/release/3.19/doxygen/tcp-socket-base_8cc_source.html (consulted on 12-02-2017)
 * https://www.nsnam.org/docs/release/3.19/doxygen/tcp-newreno_8cc_source.html (consulted on 12-02-2017)
 *
 */
public class NewRenoTcpSocket extends Socket {

    private TcpLogger tcpLogger;

    ////////////////////////////////////////////////////////
    /// TCP CONSTANTS

    // First sequence number
    private static final long FIRST_SEQ_NUMBER = 0;

    // Maximum flow size allowed in bytes
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

    // Minimum slow start threshold
    protected final long MINIMUM_SSTHRESH;

    // Loss window
    protected final long LOSS_WINDOW_SIZE;


    ////////////////////////////////////////////////////////
    /// TCP STATE

    private State currentState;          // state:       Current TCP state
    protected double slowStartThreshold; // ssthresh:    Threshold for congestion window when it goes to congestion avoidance phase
    protected double congestionWindow;   // cwnd:        Congestion window size, maximum size defined by network
    protected long sendUnackNumber;      // SND.UNA:     First number which is in window, still unacknowledged
    private long sendNextNumber;         // SND.NXT:     Next sequence number to be used
    private long receiveNextNumber;      // RCV.NXT:     Next sequence number that needs to be received to move window
    private long highestSentOutNumber;   // SND.MAXNXT:  The highest byte sequence number sent out (exclusive)

    // Duplicate acknowledgment detection variables
    private long counterSameAckNumber;
    private long previousAckNumber;

    // Selective acknowledgment variables
    private final AckRangeSet selectiveAckSet;
    private final Set<Long> acknowledgedSegStartSeqNumbers;
    private final Set<Long> sentOutUnacknowledgedSegStartSeqNumbers;

    // Fast re-transmit variables
    private long recover;
    private boolean inFastRecovery;

    // Retransmission time-out variables
    private TcpRetransmissionTimeOutEvent retransmissionTimeOutEvent;
    private boolean firstRttMeasurement;
    private double smoothRoundTripTime;
    private double roundTripTimeVariation;
    protected long roundTripTimeout; // TODO: This should be set to private actually, compatibility with Voijslav's code

    // Flowlet tracking
    private long currentFlowlet;


    ////////////////////////////////////////////////////////
    /// TCP IMPLEMENTATION

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
    public NewRenoTcpSocket(TransportLayer transportLayer, long flowId, int sourceId, int destinationId, long flowSizeByte) {
        super(transportLayer, flowId, sourceId, destinationId, flowSizeByte);

        // Load in TCP constant parameters

        // The default round-trip time-out is set rather large:
        //
        // Let's say it takes a 12-hop path (incl. ACK) with two bottlenecks on route
        // Each bottleneck takes: 150000 * 8 / 10 = 120000ns to cross
        // Each hop takes: 20ns (delay) + 1200ns = 1220ns
        //
        // So: 2*120000 + 12 * 1220 = 254640ns ~= 300000ns = 300 microseconds
        this.roundTripTimeout = Simulator.getConfiguration().getLongPropertyWithDefault("TCP_ROUND_TRIP_TIMEOUT_NS", 300000L);

        // Ethernet: 1500 - 60 (TCP header) - 60 (IP header) = 1380 bytes
        this.MAX_SEGMENT_SIZE = Simulator.getConfiguration().getLongPropertyWithDefault("TCP_MAX_SEGMENT_SIZE", 1380L);

        // Conservative slow start threshold of 30 segments, which is 33120 bytes
        long INITIAL_SLOW_START_THRESHOLD = Simulator.getConfiguration().getLongPropertyWithDefault("TCP_INITIAL_SLOW_START_THRESHOLD", 30 * MAX_SEGMENT_SIZE);

        // Maximum window size is 2^16-1 bytes, which is what is the maximum allowed in the TCP header
        this.MAX_WINDOW_SIZE = Simulator.getConfiguration().getLongPropertyWithDefault("TCP_MAX_WINDOW_SIZE", 65535L);

        // Loss window is one segment (by default)
        this.LOSS_WINDOW_SIZE = Simulator.getConfiguration().getLongPropertyWithDefault("TCP_LOSS_WINDOW_SIZE", MAX_SEGMENT_SIZE);

        // Minimum slow start threshold is 2 segments
        this.MINIMUM_SSTHRESH = Simulator.getConfiguration().getLongPropertyWithDefault("TCP_MINIMUM_SSTHRESH", 2 * MAX_SEGMENT_SIZE);

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

        // Illegal slow-start threshold relative to minimum window size
        if (this.MINIMUM_SSTHRESH < this.LOSS_WINDOW_SIZE) {
            throw new IllegalArgumentException("Minimum slow-start threshold " + MINIMUM_SSTHRESH + " must be greater than or equal to the loss window size " + LOSS_WINDOW_SIZE + ".");
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
        this.firstRttMeasurement = true;
        this.retransmissionTimeOutEvent = null;
        this.roundTripTimeVariation = 0;
        this.smoothRoundTripTime = 0;

        // Selective acknowledgments saved
        this.acknowledgedSegStartSeqNumbers = new HashSet<>();
        this.sentOutUnacknowledgedSegStartSeqNumbers = new HashSet<>();
        this.selectiveAckSet = new AckRangeSet();

        // Duplicate acknowledgment detection
        this.counterSameAckNumber = 0;
        this.previousAckNumber = -1;

        // Fast re-transmit / fast recovery
        this.recover = -1;
        this.inFastRecovery = false;

        // Start at flowlet 0
        this.currentFlowlet = 0;

        // TCP logger
        this.tcpLogger = new TcpLogger(flowId, flowSizeByte == -1);

    }

    /**
     * Determine the current flight size.
     *
     * FlightSize = SND.MAXNXT - SND.UNA
     *
     * @return  Current amount of bytes in flight (not acknowledged)
     */
    private long flightSize() {
        return sendNextNumber - sendUnackNumber;
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

    }

    @Override
    public void handle(Packet genericPacket) {
        FullExtTcpPacket packet = (FullExtTcpPacket) genericPacket;

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

                    // The syn packet we sent is now acknowledged which is 1 "byte" of information (the SYN bit)
                    sendUnackNumber += 1L;

                    // We no longer need to resend, as we've received acknowledgment
                    resetRetransmissionTimeOutTimer();

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
                    fillWindow();

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

                    // Last part of three-way handshake is confirmed
                    sendUnackNumber += 1L; // Single byte of SYN information was acknowledged

                    // Does not need to resend its SYN+ACK
                    resetRetransmissionTimeOutTimer();

                    // We don't acknowledge an acknowledgment
                    // so we don't send anything here

                    // Connection is now established
                    currentState = ESTABLISHED;

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
     * Confirm the successful confirmation of a segment by a certain packet.
     *
     * @param seq               Sequence number of segment acknowledged
     */
    private void confirmSegment(long seq) {

        // It is no longer outstanding
        sentOutUnacknowledgedSegStartSeqNumbers.remove(seq);

        // Add it to the progression
        acknowledgedSegStartSeqNumbers.add(seq);

    }

    /**
     * Process the acknowledgment as is normal for TCP.
     * It cancels the resend for any packet until then, as
     * it has arrived. Then it moves SND.UNA to make space
     * to send new packets. It then tries to fill up the
     * create space by sending out packets.
     *
     * It assumes that any congestion mechanisms have already
     * been triggered, which limit the congestion window.
     *
     * @param ack      The last acknowledged byte's sequence number
     * @param isECE    True iff the packet that acknowledged this had the ECE flag
     */
    protected void processAcknowledgment(long ack, boolean isECE) {

        // FLOW ACKNOWLEDGMENT: REGULAR
        // If it is not at the left most of the window, then
        // the receiver apparently already received everything before that
        Set<Long> outstandingSegmentSeqs = new HashSet<>(sentOutUnacknowledgedSegStartSeqNumbers);
        for (Long segSeq : outstandingSegmentSeqs) {
            long size = getFlowSizeByte(segSeq);
            long segAck = segSeq + size;
            if (segAck <= ack) {
                confirmSegment(segSeq);
            }
        }

        // MOVE WINDOW AS FAR AS POSSIBLE
        // Continue on with the other packets that have also have been
        // received already
        while (acknowledgedSegStartSeqNumbers.contains(sendUnackNumber)) {
            acknowledgedSegStartSeqNumbers.remove(sendUnackNumber);

            // Retrieve size of the already out-of-order (selectively) acknowledged packet
            long size = getFlowSizeByte(sendUnackNumber);

            // A packet's data is thus acknowledged
            this.confirmFlow(size);

            // Consume the window further
            sendUnackNumber += size;

        }

        // The window that we want to send can't start at already acknowledged numbers
        sendNextNumber = Math.max(sendNextNumber, sendUnackNumber);

    }

    /**
     * Fast-retransmit the packet starting with the given sequence number.
     *
     * @param seq   Sequence number of packet to resend
     */
    private void fastRetransmit(long seq) {

        // Fast re-transmit the data packet
        sendNextNumber = Math.max(sendNextNumber, seq); // In case a time-out occurred, and we are in fast retransmission mode
        sendOutDataPacket(seq, getFlowSizeByte(seq));

        // Log
        SimulationLogger.increaseStatisticCounter("TCP_FAST_RETRANSMIT");

    }

    /**
     * Perform the congestion control actions that are fit
     * with receiving a new acknowledgment.
     *
     * @param ack   Acknowledgment number
     * @param isECE True iff the packet was marked with ECE flag
     */
    private void handleNewAcknowledgment(long ack, boolean isECE) {

        // Partial acknowledgment: not fully recovered
        if (this.inFastRecovery && ack < this.recover) {

            this.congestionWindow -= ack - sendUnackNumber; // Determine how much has actually been newly acknowledged
                                                            // by the fast retransmissions

            this.congestionWindow += MAX_SEGMENT_SIZE; // Accounts for the one ack that we are getting right now

            this.congestionWindow = Math.max(LOSS_WINDOW_SIZE, this.congestionWindow);

            // Re-transmit the first one, which is very likely to be lost
            fastRetransmit(ack);

        // Full acknowledgment: fully recovered, exits recovery phase
        } else if (this.inFastRecovery && ack >= this.recover) {

            this.congestionWindow = Math.min(this.slowStartThreshold, flightSize() + MAX_SEGMENT_SIZE);
            this.inFastRecovery = false;
            phaseIncrementCongestionWindow(isECE);

        // Not in fast recovery at all
        } else {
            phaseIncrementCongestionWindow(isECE);
        }

    }

    /**
     * Perform the congestion control actions that are fit
     * with receiving a duplicate acknowledgment.
     *
     * @param ack   Acknowledgment number
     */
    private void handleDuplicateAcknowledgment(long ack, long count) {

        // FAST RECOVERY AND FAST RE-TRANSMIT
        // If we receive the same acknowledgment number three times in a row,
        // we assume that the packet coming after it has been lost, and re-transmit it
        if (count == 3 && !inFastRecovery && ack - 1 > recover) {

            // Adjust congestion control
            this.slowStartThreshold = Math.max(this.flightSize() / 2, MINIMUM_SSTHRESH);
            this.congestionWindow = this.slowStartThreshold + 3 * MAX_SEGMENT_SIZE;

            // Recovery threshold
            this.recover = this.highestSentOutNumber;
            this.inFastRecovery = true;

            // Re-transmit the one which is very likely to be lost
            fastRetransmit(ack);

        } else if (inFastRecovery) {
            this.congestionWindow += MAX_SEGMENT_SIZE;

        } else if (count <= 2) { // !inFastRecovery

            // RFC3042: Limited transmit; for each of the two duplicate
            //          acknowledgment till three, transmit a new segment
            while (acknowledgedSegStartSeqNumbers.contains(sendNextNumber)) {
                sendNextNumber += getFlowSizeByte(sendNextNumber);
            }
            long size = getFlowSizeByte(sendNextNumber);
            if (size > 0) {
                sendOutDataPacket(sendNextNumber, getFlowSizeByte(sendNextNumber));
            }

        }

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
            assert(ack == sendNextNumber && sendNextNumber == sendUnackNumber && packet.getDataSizeByte() == 0);
            return;
        }

        // Flowlet recording
        if (packet.getEchoFlowletId() < currentFlowlet) {
            SimulationLogger.increaseStatisticCounter("TCP_FLOWLET_OUT_OF_ORDER");
        } else {
            currentFlowlet = packet.getEchoFlowletId();
            tcpLogger.logMaxFlowlet(currentFlowlet);
        }

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
        roundTripTimeout = (long) (smoothRoundTripTime + 4 * roundTripTimeVariation);

        // Whether it acknowledges anything new
        boolean newAck = false;

        // If all flow is confirmed, we do not handle any more acknowledgments as sender
        if (this.isAllFlowConfirmed()) {
            return;
        }

        // FLOW ACKNOWLEDGMENT: SELECTIVE
        // Go over all outstanding segments and check if any are
        // being acknowledged by the selective acknowledgment ranges
        Set<Long> outstandingSegmentSeqs = new HashSet<>(sentOutUnacknowledgedSegStartSeqNumbers);
        for (Long segSeq : outstandingSegmentSeqs) {
            long size = getFlowSizeByte(segSeq);
            long segAck = segSeq + size;

            // Check if an outstanding segment falls within the selective acknowledgment ranges
            for (AckRange r : packet.getSelectiveAck()) {
                if (r.isWithin(segSeq, segAck)) {
                    confirmSegment(segSeq);
                }
            }

        }

        // DUPLICATE CHECK
        // Check if it is a duplicate acknowledgment
        if (ack == this.previousAckNumber) {
            assert(ack == this.sendUnackNumber);
            this.counterSameAckNumber++;
            handleDuplicateAcknowledgment(ack, this.counterSameAckNumber);

        // If it is a new acknowledgment
        } else if (ack > this.sendUnackNumber) {
            this.previousAckNumber = ack;
            this.counterSameAckNumber = 0;
            handleNewAcknowledgment(ack, packet.isECE());
            newAck = true;
        }

        // As default, process the acknowledgment
        processAcknowledgment(ack, packet.isECE());

        // TRANSMISSION
        // Send out as much as possible
        fillWindow();

        // Reset retransmission time-out if there is a new acknowledgment
        if (newAck && !inFastRecovery) {
            this.resetRetransmissionTimeOutTimer();
        }

        // Log congestion window
        tcpLogger.logCongestionWindow(congestionWindow);

        // FINISH
        // Flow is finished if nothing is sent and everything
        // has been acknowledged
        if (sendUnackNumber == sendNextNumber) {
            assert(flightSize() == 0);
            assert(retransmissionTimeOutEvent == null);
            assert(acknowledgedSegStartSeqNumbers.isEmpty());
            assert(sentOutUnacknowledgedSegStartSeqNumbers.isEmpty());
            assert(isAllFlowConfirmed());
        }

    }

    /* Primary debugging function:

    private void printf(String s) {
        if (flowId == 4) {
            System.out.println(s);
        }
    }

    */

    /**
     * In ESTABLISHED state, handle the reception of
     * a data packet (which needs to be acknowledged).
     *
     * @param packet    TCP packet instance
     */
    private void handleDataPacket(TcpPacket packet) {

        // Invariants
        // The receiver is always at FIRST_SEQ_NUMBER+1, having sent only the ACK+SYN message
        // For the sender, if the ACK message did not arrive, it will receive
        // another ACK+SYN message, which it will then again confirm.
        // It is possible that the sender is not at FIRST_SEQ_NUMBER+1, because the receiver send a retransmit of ACK+SYN,
        // right before receiving the final ACK. This final ACK could then go faster than the retransmitted ACK+SYN,
        // arrive earlier and thus the sender has a higher sendUnackNumber when it arrives.
        assert(this.sendUnackNumber == FIRST_SEQ_NUMBER + 1 || (!this.isReceiver() && packet.isSYN() && packet.isACK()));

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
     * Fill the congestion window by sending out as much
     * packets as is permitted given the existing window
     * and the packets already sent out but not yet confirmed.
     */
    private void fillWindow() {
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

    /**
     * Increase the congestion window if is either in slow-start or in congestion-avoidance phase.
     *
     * If cwnd < ssthresh
     *   cwnd += mss
     * else
     *   cwnd += mss * mss / cwnd
     *
     * @param   isECE   True iff the packet that sparks this increment was marked as ECE
     *
     */
    protected void phaseIncrementCongestionWindow(boolean isECE) {

        // Slow start, scales exponentially: for every packet
        // confirmed, it increases the window with one more
        // outstanding packet and so forth.
        if (this.congestionWindow < slowStartThreshold) {
            this.congestionWindow += MAX_SEGMENT_SIZE;

        // Congestion avoidance, scales linearly
        } else {
            this.congestionWindow += MAX_SEGMENT_SIZE_SQUARED / this.congestionWindow;
        }

        // Limit to maximum window size
        this.congestionWindow = Math.min(this.congestionWindow, MAX_WINDOW_SIZE);

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
        transportLayer.send(packet);
    }

    /**
     * Guarantees the sending of the TCP packet by
     * having a continuous resend event happening.
     *
     * @param tcpPacket     TCP packet instance
     */
    private void sendWithResend(TcpPacket tcpPacket) {
        resetRetransmissionTimeOutTimer();
        transportLayer.send(tcpPacket);
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
     * Reset the RTO timer because new data has been sent out.
     */
    private void resetRetransmissionTimeOutTimer() {

        // Cancel timer if it is running
        if (retransmissionTimeOutEvent != null) {
            retransmissionTimeOutEvent.cancel();
        }

        // If all outstanding data is acknowledged (per RFC 6298), we do not schedule a new RTO.
        if (sendUnackNumber == sendNextNumber) {
            retransmissionTimeOutEvent = null;

        // If there is outstanding data, reset timer
        } else {
            retransmissionTimeOutEvent = new TcpRetransmissionTimeOutEvent(roundTripTimeout, this);
            Simulator.registerEvent(retransmissionTimeOutEvent);
        }

    }

     /**
     * Handle the happening of a resend in the connection.
     *
     * Regular TCP sets the slow start threshold to half the congestion window,
     * and start the congestion window at the loss window.
     *
     * ssthresh = max (FlightSize / 2, 2*SMSS)
     * cwnd = LW
     *
     */
    public void handleRetransmissionTimeOut() {

        // Take congestion control measures
        SimulationLogger.increaseStatisticCounter("TCP_RETRANSMISSION_TIMEOUT");
        this.inFastRecovery = false;

        // Adjust slow start threshold and congestion window accordingly
        this.slowStartThreshold = Math.max(this.flightSize() / 2.0, MINIMUM_SSTHRESH);
        this.congestionWindow = LOSS_WINDOW_SIZE;

        // Per RFC 6298
        this.firstRttMeasurement = true; // Current estimate is probably bad
        this.roundTripTimeout = 2 * this.roundTripTimeout;

        // Re-transmit of SYN packet
        if (sendUnackNumber == FIRST_SEQ_NUMBER && !this.isReceiver()) {

            sendWithResend(createPacket(
                    0, // Data size (byte)
                    sendUnackNumber, // Seq number
                    0, // Ack number
                    false, // ACK
                    true,  // SYN
                    false  // ECE
            ));

        // Re-transmit of ACK+SYN packet
        } else if (sendUnackNumber == FIRST_SEQ_NUMBER && this.isReceiver()) {

            sendWithResend(createPacket(
                    0, // Data size (byte)
                    sendUnackNumber, // Seq number
                    sendUnackNumber + 1, // Ack number
                    true, // ACK
                    true, // SYN
                    false // ECE
            ));

        // Else it is just a data packet that timed out
        } else {

            // Start retransmission of all segments
            sendNextNumber = sendUnackNumber; // We can completely start transmitting again
            fillWindow();

        }

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
    protected FullExtTcpPacket createPacket(
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

}
