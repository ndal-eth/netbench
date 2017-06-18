package ch.ethz.systems.netbench.xpt.tcpbase;

import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.PacketHeader;

public interface EchoHeader extends PacketHeader {

    /**
     * Set the echo departure time (ns since simulation epoch).
     *
     * @param echoDepartureTime Echo departure time
     */
    Packet setEchoDepartureTime(long echoDepartureTime);

    /**
     * Get the echo departure time (ns since simulation epoch).
     *
     * @return  Echo departure time
     */
    long getEchoDepartureTime();

    /**
     * Set the echo flowlet identifier.
     *
     * @param echoFlowletId     Flowlet identifier of the packet this one acknowledges
     */
    Packet setEchoFlowletId(int echoFlowletId);

    /**
     * Get the echo flowlet identifier.
     *
     * @return  Flowlet identifier of the packet this one acknowledges
     */
    int getEchoFlowletId();

}
