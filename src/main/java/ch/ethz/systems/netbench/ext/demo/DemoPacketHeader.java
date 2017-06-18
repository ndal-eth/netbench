package ch.ethz.systems.netbench.ext.demo;

public interface DemoPacketHeader {

    /**
     * Get the amount of data in bytes carried by the packet.
     *
     * @return  Data carried in bytes
     */
    long getDataSizeByte();

    /**
     * Get the amount of bytes this packet acknowledges.
     *
     * @return  Number of bytes acknowledged
     */
    long getAckSizeByte();

}
