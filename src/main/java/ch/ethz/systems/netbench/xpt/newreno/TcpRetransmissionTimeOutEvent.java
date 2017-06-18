package ch.ethz.systems.netbench.xpt.newreno;

import ch.ethz.systems.netbench.core.network.Event;
import ch.ethz.systems.netbench.xpt.newreno.newrenotcp.NewRenoTcpSocket;

public class TcpRetransmissionTimeOutEvent extends Event {

    private final NewRenoTcpSocket tcpSocket;
    private boolean active;

    public TcpRetransmissionTimeOutEvent(long timeFromNowNs, NewRenoTcpSocket tcpSocket) {
        super(timeFromNowNs);
        this.tcpSocket = tcpSocket;
        this.active = true;
    }

    @Override
    public void trigger() {
        if (this.active) {
            tcpSocket.handleRetransmissionTimeOut();
        }
    }

    /**
     * Cancel the resend event. This is done when popping it from the queue takes
     * too much time, but it shouldn't occur. Scenarios in which this happens is
     * when a packet has been acknowledged or a fast retransmit is performed.
     */
    public void cancel() {
        this.active = false;
    }

    @Override
    public String toString() {
        return "TcpRetransmissionTimeOutEvent<" + this.getTime() + ", active: " + this.active + ">";
    }

}
