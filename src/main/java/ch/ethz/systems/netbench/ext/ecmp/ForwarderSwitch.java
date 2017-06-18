package ch.ethz.systems.netbench.ext.ecmp;

import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.basic.IpHeader;

/**
 * Forwarder switch.
 *
 * Very basic type of switch that looks up a destination address
 * and then only picks a single next network device based on that
 * destination address.
 */
public class ForwarderSwitch extends NetworkDevice {

    // Routing table
    private int[] destinationToNextSwitch;

    /**
     * Constructor for single forwarder switch.
     *
     * @param identifier        Network device identifier
     * @param transportLayer    Underlying server transport layer instance (set null, if none)
     * @param n                 Number of network devices in the entire network (for routing table size)
     * @param intermediary      Flowlet intermediary instance (takes care of hash adaptation for flowlet support)
     */
    ForwarderSwitch(int identifier, TransportLayer transportLayer, int n, Intermediary intermediary) {
        super(identifier, transportLayer, intermediary);
        this.destinationToNextSwitch = new int[n];
    }

    @Override
    public void receive(Packet genericPacket) {

        // Convert to IP packet
        IpHeader ipHeader = (IpHeader) genericPacket;

        if (ipHeader.getDestinationId() == this.identifier) { // Check if it has arrived

            // Hand to the underlying transport layer to the server
            this.passToIntermediary(genericPacket); // Will throw null-pointer if this network device does not have a server attached to it

        } else { // Else, it has not arrived

            // Forward to the next switch
            this.targetIdToOutputPort.get(destinationToNextSwitch[ipHeader.getDestinationId()]).enqueue(genericPacket);

        }

    }

    @Override
    public void receiveFromIntermediary(Packet genericPacket) {
        receive(genericPacket);
    }

    /**
     * Set the entry in the routing table to which next switch (hop) a packet
     * with the given destination should be sent.
     *
     * @param destinationId     Destination identifier
     * @param nextHopId         Next hop
     */
    void setDestinationToNextSwitch(int destinationId, int nextHopId) {
        if (!connectedTo.contains(nextHopId)) {
            throw new IllegalArgumentException("Cannot add hop to a network device to which it is not connected (" + nextHopId + ")");
        }
        this.destinationToNextSwitch[destinationId] = nextHopId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ForwarderSwitch<id=");
        builder.append(getIdentifier());
        builder.append(", connected=");
        builder.append(connectedTo);
        builder.append(", routing: ");
        for (int i = 0; i < destinationToNextSwitch.length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(i);
            builder.append("->");
            builder.append(destinationToNextSwitch[i]);
        }
        builder.append(">");
        return builder.toString();
    }

}
