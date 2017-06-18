package ch.ethz.systems.netbench.ext.demo;

import ch.ethz.systems.netbench.ext.basic.IpPacket;

class DemoPacket extends IpPacket implements DemoPacketHeader {

    private final long dataSizeByte;
    private final long ackSizeByte;

    DemoPacket(long flowId, long dataSizeByte, int sourceId, int destinationId, int TTL, long ackSizeByte) {
        super(flowId, dataSizeByte * 8, sourceId, destinationId, TTL);
        this.dataSizeByte = dataSizeByte;
        this.ackSizeByte = ackSizeByte;
    }

    @Override
    public long getDataSizeByte() {
        return dataSizeByte;
    }

    @Override
    public long getAckSizeByte() {
        return ackSizeByte;
    }

}
