package ch.ethz.systems.netbench.ext.bare;

import ch.ethz.systems.netbench.core.network.Event;

public class BarePacketResendEvent extends Event {

    private BareSocket bareSocket;
    private BarePacket packet;
    private boolean active;

    BarePacketResendEvent(long timeFromNowNs, BarePacket packet, BareSocket bareSocket) {
        super(timeFromNowNs);
        this.packet = packet;
        this.bareSocket = bareSocket;
        this.active = true;
    }

    @Override
    public void trigger() {
        if (this.active) {
            bareSocket.resend(packet);
        }
    }

    @Override
    public String toString() {
        return "TcpPacketResendEvent<" + this.active + ", " + this.getTime() + ", " + this.packet + ">";
    }

    /**
     * Cancel the resend event. This is done when popping it from the queue takes
     * too much time, but it shouldn't occur. Scenarios in which this happens is
     * when a packet has been acknowledged or a fast retransmit is performed.
     */
    void cancel() {
        this.bareSocket = null;
        this.packet = null;
        this.active = false;
    }

}