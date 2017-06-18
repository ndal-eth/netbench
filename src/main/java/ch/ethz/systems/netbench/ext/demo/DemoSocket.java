package ch.ethz.systems.netbench.ext.demo;

import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.Socket;
import ch.ethz.systems.netbench.core.network.TransportLayer;

/**
 * Basic socket, whose behavior is as follows. (a) The sender sends
 * out a data packet, (b) the receiver acknowledges the packet by
 * sending an ACK packet back, (c) the sender receives the ACK packet
 * and responds with a new data packet if available.
 *
 * This basic socket assumes a perfect medium.
 */
public class DemoSocket extends Socket {

    private static final long MAX_PACKET_PAYLOAD_BYTE = 1000L;

    /**
     * Create a socket. By default, it should be the receiver.
     * Use the {@link #start() start} method to make the socket a
     * sender and initiate the communication handshake.
     *
     * @param transportLayer Transport layer
     * @param flowId         Flow identifier
     * @param sourceId       Source network device identifier
     * @param destinationId  Target network device identifier
     * @param flowSizeByte   Size of the flow in bytes
     */
    DemoSocket(TransportLayer transportLayer, long flowId, int sourceId, int destinationId, long flowSizeByte) {
        super(transportLayer, flowId, sourceId, destinationId, flowSizeByte);
    }

    @Override
    public void start() {

        // Send out single data packet at start
        transportLayer.send(new DemoPacket(flowId, getNextPayloadSizeByte(), sourceId, destinationId, 100, 0));

    }

    @Override
    public void handle(Packet genericPacket) {
        DemoPacket packet = (DemoPacket) genericPacket;

        // As receiver, send an ACK packet back to acknowledge reception
        if (this.isReceiver()) {
            transportLayer.send(new DemoPacket(flowId, 0, sourceId, destinationId, 100, packet.getDataSizeByte()));

        // As sender, save that flow is confirmed, and send another packet out
        } else {
            confirmFlow(packet.getAckSizeByte());
            if (!isAllFlowConfirmed()) {
                transportLayer.send(new DemoPacket(flowId, getNextPayloadSizeByte(), sourceId, destinationId, 100, 0));
            }
        }

    }

    /**
     * Determine the payload size of the next packet.
     *
     * @return  Next payload size in bytes
     */
    private long getNextPayloadSizeByte() {
        return Math.min(MAX_PACKET_PAYLOAD_BYTE, getRemainderToConfirmFlowSizeByte());
    }

}
