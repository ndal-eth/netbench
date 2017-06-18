package ch.ethz.systems.netbench.core.network;

public interface PacketHeader {

    /**
     * Retrieve immutable flow identifier.
     *
     * @return  Flow identifier
     */
    long getFlowId();

    /**
     * Retrieve the size of the packet in bits.
     *
     * @return  Total size of the packet in bits
     */
    long getSizeBit();

    /**
     * Retrieve the departure time (equal to the construction time)
     * of the packet.
     *
     * @return  Departure time in ns since simulation epoch
     */
    long getDepartureTime();

    /**
     * Set the flowlet identifier of this packet.
     *
     * @param flowletId     Flowlet identifier to be set
     */
    void setFlowletId(int flowletId);

    /**
     * Retrieve the flowlet identifier of this packet.
     *
     * @return  Flowlet identifier
     */
    int getFlowletId();

}
