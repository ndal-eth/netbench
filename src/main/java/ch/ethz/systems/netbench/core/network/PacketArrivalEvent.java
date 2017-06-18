package ch.ethz.systems.netbench.core.network;

/**
 * Event for the complete arrival of a packet in its entirety.
 */
public class PacketArrivalEvent extends Event {

    private final NetworkDevice arrivalNetworkDevice;
    private final Packet packet;

    /**
     * Packet arrival event constructor.
     *
     * @param timeFromNowNs             Time in simulation nanoseconds from now
     * @param packet                    Packet instance which will arrive
     * @param arrivalNetworkDevice      Network device at which the packet arrives
     */
    PacketArrivalEvent(long timeFromNowNs, Packet packet, NetworkDevice arrivalNetworkDevice) {
        super(timeFromNowNs);
        this.packet = packet;
        this.arrivalNetworkDevice = arrivalNetworkDevice;
    }

    @Override
    public void trigger() {
        arrivalNetworkDevice.receive(packet);
    }

    @Override
    public String toString() {
        return "PacketArrivalEvent<" + arrivalNetworkDevice.getIdentifier() + ", " + this.getTime() + ", " + this.packet + ">";
    }

}
