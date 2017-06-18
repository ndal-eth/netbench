package ch.ethz.systems.netbench.xpt.newreno.newrenotcp;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.xpt.simple.simpletcp.SimpleTcpTransportLayer;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TcpBaseTest {

    @Mock
    private NetworkDevice networkDeviceSender;

    @Mock
    private NetworkDevice networkDeviceReceiver;

    @Mock
    private NetworkDevice networkDevice;

    @Mock
    private TcpPacket packet;

    @Test
    public void testSend() {
        SimpleTcpTransportLayer layer = new SimpleTcpTransportLayer(88);
        layer.setNetworkDevice(networkDevice);
        layer.send(packet);
        verify(networkDevice, times(1)).receiveFromTransportLayer(packet);
    }

    @Test
    public void testFlawlessThreeWayHandshakeAndTinyFlow() {

        // Set-up

        Simulator.setup(0, new NBProperties(
                BaseAllowedProperties.LOG,
                BaseAllowedProperties.PROPERTIES_RUN,
                BaseAllowedProperties.EXPERIMENTAL
        ));
        ArgumentCaptor<Packet> packetCaptor = ArgumentCaptor.forClass(Packet.class);

        // Create the layers and attach mocked network devices
        SimpleTcpTransportLayer senderLayer = new SimpleTcpTransportLayer(88);
        SimpleTcpTransportLayer receiverLayer = new SimpleTcpTransportLayer(77);
        senderLayer.setNetworkDevice(networkDeviceSender);
        receiverLayer.setNetworkDevice(networkDeviceReceiver);

        // Start a flow from 88 to 77 of size 1000
        senderLayer.startFlow(77, 1000);

        // Make sure that the first SYN of the three-way handshake is sent
        verify(networkDeviceSender, times(1)).receiveFromTransportLayer(packetCaptor.capture());
        TcpPacket synPacket = (TcpPacket) packetCaptor.getAllValues().get(0);
        assertTrue(synPacket.isSYN());
        assertFalse(synPacket.isACK());
        assertEquals(0, synPacket.getDataSizeByte());
        assertEquals(88, synPacket.getSourceId());
        assertEquals(77, synPacket.getDestinationId());
        Mockito.reset(networkDeviceSender);

        // Now pass SYN to the receiver
        receiverLayer.receive(synPacket);

        // Make sure that the second ACK+SYN of the three-way handshake is sent
        verify(networkDeviceReceiver, times(1)).receiveFromTransportLayer(packetCaptor.capture());
        TcpPacket ackSynPacket = (TcpPacket) packetCaptor.getAllValues().get(1);
        assertTrue(ackSynPacket.isSYN());
        assertTrue(ackSynPacket.isACK());
        assertEquals(0, ackSynPacket.getDataSizeByte());
        assertEquals(77, ackSynPacket.getSourceId());
        assertEquals(88, ackSynPacket.getDestinationId());
        Mockito.reset(networkDeviceReceiver);

        // Now pass ACK+SYN to the sender
        senderLayer.receive(ackSynPacket);

        verify(networkDeviceSender, times(2)).receiveFromTransportLayer(packetCaptor.capture());

        // Make sure that the third ACK of the three-way handshake is sent...
        TcpPacket ackPacket = (TcpPacket) packetCaptor.getAllValues().get(2);
        assertFalse(ackPacket.isSYN());
        assertTrue(ackPacket.isACK());
        assertEquals(0, ackPacket.getDataSizeByte());
        assertEquals(88, ackPacket.getSourceId());
        assertEquals(77, ackPacket.getDestinationId());

        // ... and the first data packet
        TcpPacket dataPacket = (TcpPacket) packetCaptor.getAllValues().get(3);
        assertFalse(dataPacket.isSYN());
        assertFalse(dataPacket.isACK());
        assertEquals(1000, dataPacket.getDataSizeByte());
        assertEquals(88, dataPacket.getSourceId());
        assertEquals(77, dataPacket.getDestinationId());

        Mockito.reset(networkDeviceSender);

        // Now pass ACK and data packet to the receiver
        receiverLayer.receive(ackPacket);
        receiverLayer.receive(dataPacket);

        verify(networkDeviceReceiver, times(1)).receiveFromTransportLayer(packetCaptor.capture());

        // Make sure that the data is acknowledged
        TcpPacket dataAckPacket = (TcpPacket) packetCaptor.getAllValues().get(4);
        assertFalse(dataAckPacket.isSYN());
        assertTrue(dataAckPacket.isACK());
        assertEquals(0, dataAckPacket.getDataSizeByte());
        assertEquals(77, dataAckPacket.getSourceId());
        assertEquals(88, dataAckPacket.getDestinationId());

        Mockito.reset(networkDeviceReceiver);

        // Now pass data ACK packet to the sender
        senderLayer.receive(dataAckPacket);

        verify(networkDeviceSender, times(0)).receiveFromTransportLayer(packetCaptor.capture());

        // Clean-up
        Simulator.reset();


    }

}
