package ch.ethz.systems.netbench.core.network;

/**
 * A intermediary is used by switches to provide the ability to modify
 * the packets that flow between the network device and the transport layer.
 * A common application is a flowlet intermediary, which adapts the flowlet
 * field of a packet.
 *
 *    /------------------\
 *    |  Network device  |
 *    \------------------/
 *       |             ^
 *       |             |
 *       v             |
 *  /-----------------------\
 *  |     Intermediary      |
 *  \-----------------------/
 *       |             ^
 *       |             |
 *       v             |
 *    /-------------------\
 *    |  Transport layer  |
 *    \-------------------/
 *
 *  There are two guarantees:
 *
 *  (a) Every packet a network device receives from the transport layer
 *      goes through the intermediary first (via {@link #adaptOutgoing(Packet) adaptOutgoing}).
 *
 *  (b) Every packet a network device sends to the transport layer
 *      goes through the intermediary first (via {@link #adaptIncoming(Packet) adaptIncoming}).
 *
 */
public abstract class Intermediary {

    // Handle to the parent network device
    private NetworkDevice networkDevice;

    /**
     * Constructor.
     * Creates the mapping tables to track flow identifiers across time.
     */
    protected Intermediary() {
        // Does not initialize state
    }

    /**
     * Adapt (or perform any action when) an incoming packet is entering
     * the network device originating from the transport layer (thus being sent out).
     * For example, modifying the flowlet field if necessary.
     *
     * @param packet    Packet instance
     *
     * @return  Packet instance (possibly with adapted flowlet field)
     */
    public abstract Packet adaptOutgoing(Packet packet);

    /**
     * Adapt (or perform any action when) an incoming packet is entering
     * the transport layer originating from the network device (thus being received).
     * For example, modifying the flowlet field if necessary.
     *
     * @param packet    Packet instance
     *
     * @return  Packet instance (possibly with adapted flowlet field)
     */
    public abstract Packet adaptIncoming(Packet packet);

    /**
     * Set the parent network device (done in the {@link NetworkDevice} constructor to ensure bi-directionality).
     *
     * @param networkDevice     Parent network device
     */
    void setNetworkDevice(NetworkDevice networkDevice) {
        assert(this.networkDevice == null);
        this.networkDevice = networkDevice;
    }

    /**
     * Retrieve the network device to which this intermediary is coupled.
     *
     * @return  Network device instance
     */
    protected NetworkDevice getNetworkDevice() {
        return networkDevice;
    }

}
