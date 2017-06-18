package ch.ethz.systems.netbench.ext.basic;

import ch.ethz.systems.netbench.core.network.PacketHeader;

public interface IpHeader extends PacketHeader {

    /**
     * Get packet source node identifier.
     *
     * @return  Source node identifier
     */
    int getSourceId();

    /**
     * Get packet destination node identifier.
     *
     * @return  Destination node identifier
     */
    int getDestinationId();

    /**
     * Check whether the packet is marked with Explicit Congestion Notification (ECN).
     *
     * @return  True iff packet is ECN-marked
     */
    boolean getECN();

    /**
     * Mark that the packet has encountered congestion (sets ECN flag).
     */
    void markCongestionEncountered();

    /**
     * Get time-to-live (in number of hops) of this packet.
     *
     * @return  Time-to-live
     */
    int getTTL();

    /**
     * Decrement the Time To Live (TTL).
     *
     * @return  True iff the TTL became zero after decrementing
     */
    boolean decrementTtlAndIsDead();

}
