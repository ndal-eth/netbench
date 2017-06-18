package ch.ethz.systems.netbench.ext.flowlet;

import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.ext.basic.TcpHeader;

/**
 * The uniform flowlet intermediary simply increases the flowlet identifier
 * each time a flowlet gap has occurred, changing the sequential hash for each packet
 * with an increment of one.
 *
 * In effect, the uniform flowlet intermediary changes the sequential hash when a flowlet
 * gap has been encountered. Whether the sequential hash actually encodes to another path is up
 * to the underlying network and the hash function it uses.
 */
public class UniformFlowletIntermediary extends FixedGapFlowletIntermediary {

    public UniformFlowletIntermediary() {
        super();
    }

    /**
     * Adapt the flowlet in the outgoing packet to the correct one for the
     * flow it is in.
     *
     * @param packet     Packet instance
     *
     * @return  Packet with adjusted flowlet identifier (e.g. used in the ecmpHash() function of a TCP packet)
     */
    @Override
    public Packet adaptOutgoing(Packet packet) {

        // Retrieve flow to which the packet belongs
        long flowId = packet.getFlowId();

        // Retrieve current flowlet of the flow
        int currentFlowlet = getCurrentFlowlet(flowId);

        // If the flowlet gap is exceeded, go to next flowlet
        if (flowletGapExceeded(flowId)) {
            currentFlowlet = Math.max(0, currentFlowlet + 1);
            setCurrentFlowlet(flowId, currentFlowlet);
        }

        // Actually set flowlet identifier on the packet
        packet.setFlowletId(currentFlowlet);

        // Set the actual hash to be dependent on both flow id and flowlet identifier
        TcpHeader tcpHeader = (TcpHeader) packet;
        tcpHeader.setHashSrcDstFlowFlowletDependent();

        // Pass on to regular packet handling
        return packet;

    }

    /**
     * Identity.
     *
     * @param packet    Packet instance
     *
     * @return  Packet instance
     */
    @Override
    public Packet adaptIncoming(Packet packet) {
        return packet;
    }

}
