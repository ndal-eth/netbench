package ch.ethz.systems.netbench.ext.valiant;

import ch.ethz.systems.netbench.ext.basic.TcpPacket;

public interface ValiantEncapsulationHeader {

    /**
     * Retrieve the encapsulated packet.
     *
     * @return  Encapsulated packet
     */
    TcpPacket getPacket();

    /**
     * Get the valiant destination.
     *
     * @return  Valiant destination
     */
    int getValiantDestination();

    /**
     * Mark that it has passed the valiant node.
     */
    void markPassedValiant();

    /**
     * Check whether it has already passed the valiant node.
     *
     * @return  True iff passed valiant (and should be directly routed to the true destination
     *          of the underlying encapsulated packet)
     */
    boolean passedValiant();

}
