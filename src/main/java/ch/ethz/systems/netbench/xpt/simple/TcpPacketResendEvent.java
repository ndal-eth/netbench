package ch.ethz.systems.netbench.xpt.simple;

import ch.ethz.systems.netbench.core.network.Event;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.simple.simpletcp.SimpleTcpSocket;

public class TcpPacketResendEvent extends Event {

    private SimpleTcpSocket tcpSocket;
    private TcpPacket tcpPacket;
    private boolean active;

    public TcpPacketResendEvent(long timeFromNowNs, TcpPacket tcpPacket, SimpleTcpSocket tcpSocket) {
        super(timeFromNowNs);
        this.tcpPacket = tcpPacket;
        this.tcpSocket = tcpSocket;
        this.active = true;
    }

    @Override
    public void trigger() {
        if (this.active) {
            tcpSocket.resend(tcpPacket);
        }
    }

    @Override
    public String toString() {
        return "TcpPacketResendEvent<" + this.active + ", " + this.getTime() + ", " + this.tcpPacket + ">";
    }

    /**
     * Cancel the resend event. This is done when popping it from the queue takes
     * too much time, but it shouldn't occur. Scenarios in which this happens is
     * when a packet has been acknowledged or a fast retransmit is performed.
     */
    public void cancel() {
        this.tcpSocket = null;
        this.tcpPacket = null;
        this.active = false;
    }

}