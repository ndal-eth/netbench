package ch.ethz.systems.netbench.ext.basic;

import ch.ethz.systems.netbench.core.network.Packet;

public abstract class IpPacket extends Packet implements IpHeader {

    // IP header is [20, 60] bytes, assume maximum: 60 * 8
    private static final long IP_HEADER_SIZE_BIT = 480L;

    // IP header fields
    private final int sourceId;
    private final int destinationId;
    private boolean ECN;
    private int TTL;

    /**
     * Internet Protocol (IP) packet constructor.
     *
     * @param flowId            Flow identifier
     * @param payloadSizeBit    Payload of the IP packet in bits
     * @param sourceId          Source node identifier
     * @param destinationId     Destination node identifier
     * @param TTL               Initial time-to-live (maximum number of hops)
     */
    public IpPacket(long flowId, long payloadSizeBit, int sourceId, int destinationId, int TTL) {
        super(flowId, IP_HEADER_SIZE_BIT + payloadSizeBit);
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.ECN = false;
        this.TTL = TTL;
    }

    /**
     * Get packet source node identifier.
     *
     * @return  Source node identifier
     */
    @Override
    public int getSourceId() {
        return sourceId;
    }

    /**
     * Get packet destination node identifier.
     *
     * @return  Destination node identifier
     */
    @Override
    public int getDestinationId() {
        return destinationId;
    }

    /**
     * Check whether the packet is marked with Explicit Congestion Notification (ECN).
     *
     * @return  True iff packet is ECN-marked
     */
    @Override
    public boolean getECN() {
        return ECN;
    }

    /**
     * Mark that the packet has encountered congestion (sets ECN flag).
     */
    @Override
    public void markCongestionEncountered() {
        this.ECN = true;
    }

    /**
     * Get time-to-live (in number of hops) of this packet.
     *
     * @return  Time-to-live
     */
    @Override
    public int getTTL() {
        return TTL;
    }

    /**
     * Decrement the Time To Live (TTL).
     *
     * @return  True iff the TTL became zero after decrementing
     */
    @Override
    public boolean decrementTtlAndIsDead() {
        assert(TTL > 0);
        TTL--;
        return TTL == 0;
    }

}
