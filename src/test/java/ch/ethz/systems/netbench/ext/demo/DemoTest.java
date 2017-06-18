package ch.ethz.systems.netbench.ext.demo;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DemoTest {

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
        Simulator.setup(0);
        DemoTransportLayerGenerator generator = new DemoTransportLayerGenerator();
        DemoTransportLayer layer = (DemoTransportLayer) generator.generate(88);
        layer.setNetworkDevice(networkDevice);
        layer.send(packet);
        verify(networkDevice, times(1)).receiveFromTransportLayer(packet);
        Simulator.reset();
    }

    @Test
    public void testTwoTimeBackForward() {

        // Set-up
        Simulator.setup(0, new NBProperties(
                BaseAllowedProperties.LOG,
                BaseAllowedProperties.PROPERTIES_RUN
        ));

        ArgumentCaptor<Packet> packetCaptor = ArgumentCaptor.forClass(Packet.class);

        // Create the layers and attach mocked network devices
        DemoTransportLayer senderLayer = new DemoTransportLayer(88);
        DemoTransportLayer receiverLayer = new DemoTransportLayer(77);
        senderLayer.setNetworkDevice(networkDeviceSender);
        receiverLayer.setNetworkDevice(networkDeviceReceiver);

        // Start a flow from 88 to 77 of size 2000
        senderLayer.startFlow(77, 2000);

        // First data packet
        verify(networkDeviceSender, times(1)).receiveFromTransportLayer(packetCaptor.capture());
        DemoPacket firstPacket = (DemoPacket) packetCaptor.getAllValues().get(0);
        assertEquals(1000, firstPacket.getDataSizeByte());
        assertEquals(88, firstPacket.getSourceId());
        assertEquals(77, firstPacket.getDestinationId());
        assertEquals(0, firstPacket.getAckSizeByte());
        Mockito.reset(networkDeviceSender);

        // Now pass first packet to the receiver
        receiverLayer.receive(firstPacket);

        // First acknowledgment packet
        verify(networkDeviceReceiver, times(1)).receiveFromTransportLayer(packetCaptor.capture());
        DemoPacket firstAckPacket = (DemoPacket) packetCaptor.getAllValues().get(1);
        assertEquals(0, firstAckPacket.getDataSizeByte());
        assertEquals(77, firstAckPacket.getSourceId());
        assertEquals(88, firstAckPacket.getDestinationId());
        assertEquals(1000, firstAckPacket.getAckSizeByte());
        Mockito.reset(networkDeviceReceiver);

        // Now pass ACK+SYN to the sender
        senderLayer.receive(firstAckPacket);

        verify(networkDeviceSender, times(1)).receiveFromTransportLayer(packetCaptor.capture());

        // Second data packet
        DemoPacket secondPacket = (DemoPacket) packetCaptor.getAllValues().get(2);
        assertEquals(1000, secondPacket.getDataSizeByte());
        assertEquals(88, secondPacket.getSourceId());
        assertEquals(77, secondPacket.getDestinationId());
        assertEquals(0, secondPacket.getAckSizeByte());
        Mockito.reset(networkDeviceSender);

        // Now pass second data packet to the receiver
        receiverLayer.receive(secondPacket);

        verify(networkDeviceReceiver, times(1)).receiveFromTransportLayer(packetCaptor.capture());

        // Make sure that the data is acknowledged
        DemoPacket secondAckPacket = (DemoPacket) packetCaptor.getAllValues().get(3);
        assertEquals(0, secondAckPacket.getDataSizeByte());
        assertEquals(77, secondAckPacket.getSourceId());
        assertEquals(88, secondAckPacket.getDestinationId());
        assertEquals(1000, secondAckPacket.getAckSizeByte());
        Mockito.reset(networkDeviceReceiver);

        // Now pass second ack to the sender
        senderLayer.receive(secondAckPacket);

        // Should not send anything more
        verify(networkDeviceSender, times(0)).receiveFromTransportLayer(packetCaptor.capture());

        // Clean-up
        Simulator.reset();


    }

}
