package ch.ethz.systems.netbench.ext.bare;

import ch.ethz.systems.netbench.ext.basic.TcpPacket;

class BarePacket extends TcpPacket {

    BarePacket(long flowId, long dataSizeByte, int sourceId, int destinationId, long sequenceNumber, long acknowledgementNumber, boolean ECE, boolean ACK, double windowSize) {
        super(
                flowId,
                dataSizeByte,
                sourceId,
                destinationId,
                0,
                0,
                0,
                sequenceNumber,
                acknowledgementNumber,
                false,
                false,
                ECE,
                false,
                ACK,
                false,
                false,
                false,
                false,
                windowSize
        );
    }

}
