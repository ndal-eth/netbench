package ch.ethz.systems.netbench.core.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstraction for a network device.
 *
 * All nodes in the network are instances of this abstraction.
 * It takes care of the definition of network connections and
 * forces its subclasses to be able to handle packets it receives.
 * A network device is a server iff it has a
 * {@link TransportLayer transport layer}.
 *
 * It enables additional modification of packets by placement of a
 * {@link Intermediary intermediary}
 * in between the network device and the transport layer.
 */
public abstract class NetworkDevice {

    private final TransportLayer transportLayer;
    private final boolean hasTransportLayer;
    protected final int identifier;
    protected final List<Integer> connectedTo;
    protected final Map<Integer, OutputPort> targetIdToOutputPort;
    protected final Intermediary intermediary;

    /**
     * Constructor of a network device.
     *
     * @param identifier        Network device identifier
     * @param transportLayer    Transport layer instance (null, if only router and not a server)
     * @param intermediary      Flowlet intermediary instance (takes care of flowlet support)
     */
    protected NetworkDevice(int identifier, TransportLayer transportLayer, Intermediary intermediary) {

        // Permanent unique identifier assigned
        this.identifier = identifier;

        // Initialize internal data structures
        this.connectedTo = new ArrayList<>();
        this.targetIdToOutputPort = new HashMap<>();

        // Set the server and whether it exists
        this.transportLayer = transportLayer;
        this.hasTransportLayer = (transportLayer != null);

        // Add intermediary
        this.intermediary = intermediary;
        this.intermediary.setNetworkDevice(this);

    }

    /**
     * Retrieve the automatically generated unique
     * network device identifier.
     *
     * @return  Unique network device identifier
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Add a port which is a connection to another network device.
     *
     * @param outputPort    Output port instance
     */
    public void addConnection(OutputPort outputPort) {

        // Port does not originate from here
        if (getIdentifier() != outputPort.getOwnId()) {
            throw new IllegalArgumentException("Impossible to add output port not originating from " + getIdentifier() + " (origin given: " + outputPort.getOwnId() + ")");
        }

        // Port going there already exists
        if (connectedTo.contains(outputPort.getTargetId())) {
            throw new IllegalArgumentException("Impossible to add a duplicate port from " + outputPort.getOwnId() + " to " + outputPort.getTargetId() + ".");
        }

        // Add to mappings
        connectedTo.add(outputPort.getTargetId());
        targetIdToOutputPort.put(outputPort.getTargetId(), outputPort);

    }

    /**
     * Check whether this network device has an outgoing port to the target.
     *
     * @param target    Target network device identifier
     *
     * @return  True iff an outgoing port from this network device exists to the target
     */
    public boolean hasConnection(int target) {
        return connectedTo.contains(target);
    }

    /**
     * Reception of a packet by the network device from another network device.
     * // TODO: make it protected-local?
     *
     * @param genericPacket    Packet instance
     */
    public abstract void receive(Packet genericPacket);

    /**
     * Reception of a packet by the network device from the underlying transport layer (adapted by flowlet
     * intermediary if necessary).
     *
     * @param genericPacket     Packet instance (with flowlet modified)
     */
    protected abstract void receiveFromIntermediary(Packet genericPacket);

    /**
     * Pass a packet to the underlying intermediary, which adapts it if needed
     * and forwards it to the transport layer of the network device.
     *
     * @param genericPacket     Packet instance
     */
    protected void passToIntermediary(Packet genericPacket) {
        intermediary.adaptIncoming(genericPacket);
        transportLayer.receive(genericPacket);
    }

    /**
     * Reception of a packet by the network device from the underlying transport layer.
     * Adapts it via the intermediary and then sends it on to the switch.
     * Do not override. // TODO: make it package-local?
     *
     * @param genericPacket    Packet instance
     */
    public void receiveFromTransportLayer(Packet genericPacket) {
        intermediary.adaptOutgoing(genericPacket);
        this.receiveFromIntermediary(genericPacket);
    }

    /**
     * Check whether the network device has a transport layer and is thus a potential server.
     * A server is a node which is able to send and/or receive traffic in the network, in essence
     * it can be a source or sink whereas normal routing nodes without transport layer cannot be.
     *
     * @return  True iff the network device is a server
     */
	public boolean isServer() {
		return hasTransportLayer;
	}

    /**
     * Retrieve the underlying transport layer of the network device.
     * Must not be called if there is no underlying transport layer (so the network device is not a server).
     *
     * @return  Transport layer
     */
    public TransportLayer getTransportLayer() {
        assert(transportLayer != null);
        return transportLayer;
    }

}
