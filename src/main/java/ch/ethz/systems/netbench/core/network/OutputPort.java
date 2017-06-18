package ch.ethz.systems.netbench.core.network;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.log.PortLogger;

import java.util.Queue;

/**
 * Abstraction for an output port on a network device.
 *
 * There is no corresponding InputPort class, as the output
 * port already forces a rate limit. OutputPort's subclasses
 * are forced to handle the enqueuing of packets, and are allowed
 * to drop packets depending on their own drop strategy to handle
 * congestion at the port (e.g. tail-drop, RED, ...).
 */
public abstract class OutputPort {

    // Internal state
    private boolean isSending;          // True iff the output port is using the medium to send a packet
    private final Queue<Packet> queue;  // Current queue of packets to send
    private long bufferOccupiedBits;    // Amount of bits currently occupied of the buffer

    // Constants
    private final int ownId;                            // Own network device identifier
    private final NetworkDevice ownNetworkDevice;       // Network device this output port is attached to
    private final int targetId;                         // Target network device identifier
    private final NetworkDevice targetNetworkDevice;    // Target network device
    private final Link link;                            // Link type, defines latency and bandwidth of the medium
                                                        // that the output port uses

    // Logging utility
    private final PortLogger logger;

    /**
     * Constructor.
     *
     * @param ownNetworkDevice      Source network device to which this output port is attached
     * @param targetNetworkDevice   Target network device that is on the other side of the link
     * @param link                  Link that this output ports solely governs
     * @param queue                 Queue that governs how packet are stored queued in the buffer
     */
    protected OutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link, Queue<Packet> queue) {

        // State
        this.queue = queue;
        this.isSending = false;
        this.link = link;
        this.bufferOccupiedBits = 0;

        // References
        this.ownNetworkDevice = ownNetworkDevice;
        this.ownId = this.ownNetworkDevice.getIdentifier();
        this.targetNetworkDevice = targetNetworkDevice;
        this.targetId = this.targetNetworkDevice.getIdentifier();

        // Logging
        this.logger = new PortLogger(this);

    }

    /**
     * Enqueue the given packet for sending.
     * There is no guarantee that the packet is actually sent,
     * as the queue buffer's limit might be reached.
     *
     * @param packet    Packet instance
     */
    public abstract void enqueue(Packet packet);

    /**
     * Enqueue the given packet.
     *
     * @param packet    Packet instance
     */
    protected final void guaranteedEnqueue(Packet packet) {

        // If it is not sending, then the queue is empty at the moment,
        // so this packet can be immediately send
        if (!isSending) {

            // Link is now being utilized
            logger.logLinkUtilized(true);

            // Add event when sending is finished
            Simulator.registerEvent(new PacketDispatchedEvent(
                    packet.getSizeBit() / link.getBandwidthBitPerNs(),
                    packet,
                    this
            ));

            // It is now sending again
            isSending = true;

        } else { // If it is still sending, the packet is added to the queue, making it non-empty
            bufferOccupiedBits += packet.getSizeBit();
            queue.add(packet);
            logger.logQueueState(queue.size(), bufferOccupiedBits);
        }

    }

    /**
     * Called when a packet has actually been send completely.
     * In response, register arrival event at the destination network device,
     * and starts sending another packet if it is available.
     *
     * @param packet    Packet instance that was being sent
     */
    void dispatch(Packet packet) {

        // Finished sending packet, the last bit of the packet should arrive the link-delay later
        if (!link.doesNextTransmissionFail(packet.getSizeBit())) {
            Simulator.registerEvent(
                    new PacketArrivalEvent(
                            link.getDelayNs(),
                            packet,
                            targetNetworkDevice
                    )
            );
        }

        // Again free to send other packets
        isSending = false;

        // Check if there are more in the queue to send
        if (!queue.isEmpty()) {

            // Pop from queue
            Packet packetFromQueue = queue.poll();
            decreaseBufferOccupiedBits(packetFromQueue.getSizeBit());
            logger.logQueueState(queue.size(), bufferOccupiedBits);

            // Register when the packet is actually dispatched
            Simulator.registerEvent(new PacketDispatchedEvent(
                    packetFromQueue.getSizeBit() / link.getBandwidthBitPerNs(),
                    packetFromQueue,
                    this
            ));

            // It is sending again
            isSending = true;

        } else {

            // If the queue is empty, nothing will be sent for now
            logger.logLinkUtilized(false);

        }

    }

    /**
     * Return the network identifier of its own device (to which this output port is attached to).
     *
     * @return  Own network device identifier
     */
    public int getOwnId() {
        return ownId;
    }

    /**
     * Return the network identifier of the target device.
     *
     * @return  Target network device identifier
     */
    public int getTargetId() {
        return targetId;
    }

    /**
     * Return the network device where this ports originates from.
     *
     * @return  Own network device
     */
    public NetworkDevice getOwnDevice(){
        return ownNetworkDevice;
    }
    
    /**
     * Return the network device at the other end of this port.
     *
     * @return  Target network device
     */
    public NetworkDevice getTargetDevice(){
    		return targetNetworkDevice;
    }

    /**
     * Retrieve size of the queue in packets.
     *
     * @return  Queue size in packets
     */
    public int getQueueSize() {
        return queue.size();
    }

    /**
     * Determine the amount of bits that the buffer occupies.
     *
     * @return  Bits currently occupied in the buffer of this output port.
     */
    protected long getBufferOccupiedBits() {
        return bufferOccupiedBits;
    }

    @Override
    public String toString() {
        return  "OutputPort<" +
                    ownId + " -> " + targetId +
                    ", link: " + link +
                    ", occupied: " + bufferOccupiedBits +
                    ", queue size: " + getQueueSize() +
                ">";
    }

    /**
     * Retrieve the queue used in the output port.
     *
     * NOTE: adapting the queue will most likely result in strange values
     *       in the port queue state log.
     *
     * @return  Queue instance
     */
    protected Queue<Packet> getQueue() {
        return queue;
    }

    /**
     * Change the amount of bits occupied in the buffer with a delta.
     *
     * NOTE: adapting the buffer occupancy counter from your own implementation
     *       will most likely result in strange values in the port queue state log.
     *
     * @param deltaAmount    Amount of bits to from the current occupied counter
     */
    protected void decreaseBufferOccupiedBits(long deltaAmount) {
        bufferOccupiedBits -= deltaAmount;
        assert(bufferOccupiedBits >= 0);
    }

}
