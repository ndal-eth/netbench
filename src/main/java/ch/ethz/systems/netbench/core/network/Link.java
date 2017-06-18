package ch.ethz.systems.netbench.core.network;

/**
 * A link is an abstraction for any (directed) connection
 * between two {@link NetworkDevice network devices}.
 */
public abstract class Link {

    /**
     * Get the time it takes in nanoseconds (ns) for a unit
     * of information to travel over the link.
     *
     * @return  Link delay in nanoseconds
     */
    public abstract long getDelayNs();

    /**
     * Get the bandwidth of the link in bits per nanosecond (1 bit/ns = 1 Gbit/s).
     *
     * @return  Link bandwidth in bit/ns
     */
    public abstract long getBandwidthBitPerNs();

    /**
     * Check whether the next transmission of a packet will fail.
     *
     * @param packetSizeBits    Size of the packet
     *
     * @return True iff it is not delivered correctly (e.g. by modeling electromagnetic interference).
     */
    public abstract boolean doesNextTransmissionFail(long packetSizeBits);

}
