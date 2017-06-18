package ch.ethz.systems.netbench.xpt.tcpbase;

import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.PacketHeader;

import java.util.Collection;

public interface SelectiveAckHeader extends PacketHeader {

    /**
     * Set the selective acknowledgment.
     *
     * @param selectiveAck  Selective acknowledgment
     */
    Packet setSelectiveAck(Collection<AckRange> selectiveAck);

    /**
     * Get the selective acknowledgment field.
     *
     * @return  Selective acknowledgment
     */
    Collection<AckRange> getSelectiveAck();

}
