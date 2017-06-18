package ch.ethz.systems.netbench.xpt.sourcerouting;

import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.basic.TcpHeader;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.ext.ecmp.EcmpSwitchRoutingInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EcmpThenSourceRoutingSwitch extends SourceRoutingSwitch implements EcmpSwitchRoutingInterface {

    // Amount of flow size sent out for each flow
    private final Map<Long, Long> flowSizeSent;

    // Threshold at which the switch goes from direct ECMP routing to valiant routing
    private final long switchThresholdBytes;

    // ECMP routing table
    private final List<List<Integer>> destinationToNextSwitch;

    /**
     * Constructor for ECMP then Source Routing switch WITH a transport layer attached to it.
     *
     * @param identifier     Network device identifier
     * @param transportLayer Underlying server transport layer instance (set null, if none)
     * @param n              Number of network devices in the entire network (for routing table size)
     * @param intermediary   Flowlet intermediary instance (takes care of hash adaptation for flowlet support)
     * @param switchThresholdBytes      Number of bytes a flow needs to send out before it goes for source routing instead of ECMP
     */
    EcmpThenSourceRoutingSwitch(int identifier, TransportLayer transportLayer, int n, Intermediary intermediary, long switchThresholdBytes) {
        super(identifier, transportLayer, n, intermediary);
        this.flowSizeSent = new HashMap<>();
        this.switchThresholdBytes = switchThresholdBytes;
        this.destinationToNextSwitch = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            this.destinationToNextSwitch.add(new ArrayList<>());
        }
    }

    @Override
    public void receive(Packet genericPacket) {

        if (genericPacket instanceof TcpHeader) {

            // Convert to TCP packet
            TcpHeader tcpHeader = (TcpHeader) genericPacket;

            // Check if it has arrived
            if (tcpHeader.getDestinationId() == this.identifier) {

                // Hand to the underlying server
                this.passToIntermediary(genericPacket); // Will throw null-pointer if this network device does not have a server attached to it

            } else {

                // Forward to the next switch
                List<Integer> possibilities = destinationToNextSwitch.get(tcpHeader.getDestinationId());
                this.targetIdToOutputPort.get(possibilities.get(tcpHeader.getHash(this.identifier) % possibilities.size())).enqueue(genericPacket);

            }

        } else if (genericPacket instanceof SourceRoutingEncapsulation) {
            super.receive(genericPacket);

        } else {
            throw new RuntimeException("Invalid packet received: " + genericPacket);
        }


    }

    @Override
    public void receiveFromIntermediary(Packet genericPacket) {
        TcpHeader tcpHeader = (TcpHeader) genericPacket;

        // Determine amount of flow already dispatched
        Long amount = flowSizeSent.get(tcpHeader.getFlowId());
        amount = amount == null ? 0L : amount;
        amount += tcpHeader.getDataSizeByte();
        flowSizeSent.put(tcpHeader.getFlowId(), amount);

        if (amount <= switchThresholdBytes) { // Under threshold, do ECMP (no encapsulation)
            this.receive(genericPacket);

        } else { // Over threshold, perform source routing
            super.receiveFromIntermediary(genericPacket);
        }

    }

    @Override
    public void addDestinationToNextSwitch(int destinationId, int nextHopId) {

        // Check for not possible identifier
        if (!connectedTo.contains(nextHopId)) {
            throw new IllegalArgumentException("Cannot add hop to a network device to which it is not connected (" + nextHopId + ")");
        }

        // Check for duplicate
        List<Integer> current = this.destinationToNextSwitch.get(destinationId);
        if (current.contains(nextHopId)) {
            throw new IllegalArgumentException("Cannot add a duplicate next hop network device identifier (" + nextHopId + ")");
        }

        // Add to current ones
        current.add(nextHopId);

    }

}
