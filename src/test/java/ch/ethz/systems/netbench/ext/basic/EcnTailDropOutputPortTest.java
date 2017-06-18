package ch.ethz.systems.netbench.ext.basic;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EcnTailDropOutputPortTest {

    private static final long packetSizeDataBytes = 1500;

    @Mock
    private NetworkDevice sourceNetworkDevice;

    @Mock
    private NetworkDevice targetNetworkDevice;

    @Mock
    private Link link;

    @Mock
    private TcpPacket packet;

    @Before
    public void setup() {

        Simulator.setup(0, new NBProperties(BaseAllowedProperties.LOG, BaseAllowedProperties.PROPERTIES_RUN));

        // Two network devices
        when(sourceNetworkDevice.getIdentifier()).thenReturn(10);
        when(targetNetworkDevice.getIdentifier()).thenReturn(67);

        // Port with 100 packets and 40 packets ECN limit
        when(link.getBandwidthBitPerNs()).thenReturn(10L);
        when(link.getDelayNs()).thenReturn(20L);

    }

    @After
    public void cleanup() {
        Simulator.reset();
    }

    @Test
    public void testFields() {
        EcnTailDropOutputPort port = new EcnTailDropOutputPort(sourceNetworkDevice, targetNetworkDevice, link, 100 * packetSizeDataBytes, 40 * packetSizeDataBytes);
        assertEquals(sourceNetworkDevice, port.getOwnDevice());
        assertEquals(targetNetworkDevice, port.getTargetDevice());
        assertEquals(10, port.getOwnId());
        assertEquals(67, port.getTargetId());
        assertNotNull(port.toString());
    }

    @Test
    public void testQueueingAndECNMarking() {
        reset(packet);
        when(packet.getSizeBit()).thenReturn(packetSizeDataBytes * 8L);

        // Port with 100 packets and 40 packets ECN limit
        EcnTailDropOutputPort port = new EcnTailDropOutputPort(sourceNetworkDevice, targetNetworkDevice, link, 100 * packetSizeDataBytes, 40 * packetSizeDataBytes);

        // First 41 packets queued should not be marked for congestion
        // 40 in queue, 1 in dispatch position
        for (int i = 0; i < 41; i++) {
            port.enqueue(packet);
        }
        verify(packet, times(0)).markCongestionEncountered();
        assertEquals(40, port.getQueueSize());

        // Next 60 packets queued should be marked for congestion and enqueued
        for (int i = 0; i < 60; i++) {
            port.enqueue(packet);
        }
        verify(packet, times(60)).markCongestionEncountered();
        assertEquals(100, port.getQueueSize());

        // Next 10 packets queued should be marked for congestion and not enqueued but thrown away
        for (int i = 0; i < 10; i++) {
            port.enqueue(packet);
        }
        verify(packet, times(70)).markCongestionEncountered();
        assertEquals(100, port.getQueueSize());

        // Only a single packet dispatch event should be in there
        assertEquals(Simulator.getEventSize(), 1);

    }

    @Test
    public void testDispatchJustNot() {

        // Standard packet
        reset(packet);
        when(packet.getSizeBit()).thenReturn(packetSizeDataBytes * 8L);

        // Port with 100 packets and 40 packets ECN limit
        EcnTailDropOutputPort port = new EcnTailDropOutputPort(sourceNetworkDevice, targetNetworkDevice, link, 100 * packetSizeDataBytes, 40 * packetSizeDataBytes);

        // Enqueue two packets
        port.enqueue(packet);
        port.enqueue(packet);
        assertEquals(1, port.getQueueSize());

        // Just before dispatch of the first packet
        Simulator.runNs(packetSizeDataBytes * 8L / 10 - 1);

        // Second packet is still in queue, still dispatch of first packet
        assertEquals(1, port.getQueueSize());
        assertEquals(1, Simulator.getEventSize());

        // None has arrived already
        verify(targetNetworkDevice, times(0)).receive(packet);

    }

    @Test
    public void testDispatchJust() {

        // Standard packet
        reset(packet);
        when(packet.getSizeBit()).thenReturn(packetSizeDataBytes * 8L);

        // Port with 100 packets and 40 packets ECN limit
        EcnTailDropOutputPort port = new EcnTailDropOutputPort(sourceNetworkDevice, targetNetworkDevice, link, 100 * packetSizeDataBytes, 40 * packetSizeDataBytes);

        // Queue two packets
        port.enqueue(packet);
        port.enqueue(packet);
        assertEquals(1, port.getQueueSize());

        // Exactly dispatch one packet, but it is not yet arrived
        Simulator.runNs(packetSizeDataBytes * 8L / 10);

        // No queue as one is now being sent
        assertEquals(0, port.getQueueSize());

        // One packet dispatch and one packet arrival event
        assertEquals(2, Simulator.getEventSize());

        // None has arrived already
        verify(targetNetworkDevice, times(0)).receive(packet);

    }

    @Test
    public void testDispatchOneSent() {

        // Standard packet size
        reset(packet);
        when(packet.getSizeBit()).thenReturn(packetSizeDataBytes * 8L);

        // Port with 100 packets and 40 packets ECN limit
        EcnTailDropOutputPort port = new EcnTailDropOutputPort(sourceNetworkDevice, targetNetworkDevice, link, 100 * packetSizeDataBytes, 40 * packetSizeDataBytes);

        // Queue two packets
        port.enqueue(packet);
        port.enqueue(packet);

        // One is queue, other is being sent
        assertEquals(1, port.getQueueSize());

        // Run such that one exactly arrives
        Simulator.runNs(packetSizeDataBytes * 8L / 10 + 20);

        // The other is now being sent
        assertEquals(0, port.getQueueSize());

        // One packet dispatch event left
        assertEquals(1, Simulator.getEventSize());

        // The other has arrived already
        verify(targetNetworkDevice, times(1)).receive(packet);

    }

    @Test
    public void testDispatchOneSentSecondJustNot() {

        // Standard packet size
        reset(packet);
        when(packet.getSizeBit()).thenReturn(packetSizeDataBytes * 8L);

        // Port with 100 packets and 40 packets ECN limit
        EcnTailDropOutputPort port = new EcnTailDropOutputPort(sourceNetworkDevice, targetNetworkDevice, link, 100 * packetSizeDataBytes, 40 * packetSizeDataBytes);

        // Queue two packets
        port.enqueue(packet);
        port.enqueue(packet);

        // One is in the queue, the other is not
        assertEquals(1, port.getQueueSize());

        // Link has delay of 20ns, and a throughput of 10 bit/ns
        // So one nanosecond is left
        Simulator.runNs(2 * packetSizeDataBytes * 8L / 10 + 19);
        assertEquals(0, port.getQueueSize());

        // One packet arrival event is left
        assertEquals(1, Simulator.getEventSize());

        // The other has arrived already
        verify(targetNetworkDevice, times(1)).receive(packet);

    }

    @Test
    public void testDispatchTwoSent() {

        // Standard packet size
        reset(packet);
        when(packet.getSizeBit()).thenReturn(packetSizeDataBytes * 8L);

        // Port with 100 packets and 40 packets ECN limit
        EcnTailDropOutputPort port = new EcnTailDropOutputPort(sourceNetworkDevice, targetNetworkDevice, link, 100 * packetSizeDataBytes, 40 * packetSizeDataBytes);

        // Enqueue two packets
        port.enqueue(packet);
        port.enqueue(packet);

        // One is in the port
        assertEquals(1, port.getQueueSize());

        // Run the simulator such that exactly two packets have arrived at the target network device
        Simulator.runNs(2 * packetSizeDataBytes * 8L / 10 + 20);

        // No events left because target network device is a mock object
        assertEquals(0, port.getQueueSize());
        assertEquals(0, Simulator.getEventSize());

        // Two have arrived now
        verify(targetNetworkDevice, times(2)).receive(packet);

    }

    @Mock
    private NetworkDevice networkDeviceA;

    @Mock
    private NetworkDevice networkDeviceB;

    @Test
    public void testGenerator() {
        EcnTailDropOutputPortGenerator generator = new EcnTailDropOutputPortGenerator(5000, 2000);
        PerfectSimpleLink link = new PerfectSimpleLink(100, 200);
        when(networkDeviceA.getIdentifier()).thenReturn(77);
        when(networkDeviceB.getIdentifier()).thenReturn(88);
        EcnTailDropOutputPort port = (EcnTailDropOutputPort) generator.generate(networkDeviceA, networkDeviceB, link);
        assertEquals(77, port.getOwnId());
        assertEquals(88, port.getTargetId());
    }

}
