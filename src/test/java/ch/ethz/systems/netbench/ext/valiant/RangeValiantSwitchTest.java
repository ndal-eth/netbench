package ch.ethz.systems.netbench.ext.valiant;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.ext.flowlet.IdentityFlowletIntermediary;
import ch.ethz.systems.netbench.testutility.TestTopologyPortsConstruction;
import ch.ethz.systems.netbench.xpt.tcpbase.FullExtTcpPacket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RangeValiantSwitchTest {

    /*
     * Topology:
     * 0---1---2
     *     |   |
     *     |   |
     *     3---4
     */
    private TestTopologyPortsConstruction topology;

    @Mock
    private TcpPacket packet;

    private RangeValiantSwitch device4identity;

    @Mock
    private TransportLayer layer4;

    @Before
    public void setup() {

        Simulator.setup(0, new NBProperties(
                BaseAllowedProperties.LOG,
                BaseAllowedProperties.PROPERTIES_RUN,
                BaseAllowedProperties.EXTENSION
        ));

        // Set flowlet gap to 0 such that uniform intermediary continuously switches
        Simulator.getConfiguration().overrideProperty("FLOWLET_GAP_NS", "0");

        // Create port topology
        topology = new TestTopologyPortsConstruction(
                "0-1,1-2,2-4,4-3,3-1"
        );

        // Initialize ECMP routing scheme for device number 4 with identity flowlet intermediary
        device4identity = new RangeValiantSwitch(4, layer4, 5, new IdentityFlowletIntermediary(), 0, 4);
        device4identity.addConnection(topology.getPort(4, 2));
        device4identity.addConnection(topology.getPort(4, 3));
        device4identity.addDestinationToNextSwitch(0, 2);
        device4identity.addDestinationToNextSwitch(0, 3);
        device4identity.addDestinationToNextSwitch(1, 2);
        device4identity.addDestinationToNextSwitch(1, 3);
        device4identity.addDestinationToNextSwitch(2, 2);
        device4identity.addDestinationToNextSwitch(3, 3);

    }

    @After
    public void cleanup() {
        Simulator.reset();
    }

    @Test
    public void testTerribleHashing() {

        // Packet destined for node 3
        when(packet.getSourceId()).thenReturn(4);
        when(packet.getDestinationId()).thenReturn(3);
        when(packet.getHash(eq(4), anyInt())).thenReturn(3);

        boolean thrown = false;
        try {
            device4identity.receiveFromIntermediary(packet);
        } catch (RuntimeException e) {
            thrown = true;
        }
        assertTrue(thrown);

    }

    @Test
    public void testValiantPass() {

        // Packet destined for node 3
        when(packet.getDestinationId()).thenReturn(3);

        // Create encapsulation for packet to valiant node 4
        ValiantEncapsulation encapsulation = new ValiantEncapsulation(packet, 4);

        // Node 4 should receive it, and decapsulate it, forward to real destination
        assertFalse(encapsulation.passedValiant());
        device4identity.receive(encapsulation);
        verify(topology.getPort(4, 2), times(0)).enqueue(encapsulation);
        verify(topology.getPort(4, 3), times(1)).enqueue(encapsulation);
        assertTrue(encapsulation.passedValiant());
        verify(layer4, times(0)).receive(packet);

    }

    @Test
    public void testValiantPassNormal() {

        // Packet destined for node 3
        when(packet.getSourceId()).thenReturn(4);
        when(packet.getDestinationId()).thenReturn(3);
        when(packet.isACK()).thenReturn(false);

        // Node 4 should receive it, and forward directly, as it is an ACK
        device4identity.receiveFromIntermediary(packet);
        ArgumentCaptor<ValiantEncapsulation> captor = ArgumentCaptor.forClass(ValiantEncapsulation.class);
        verify(topology.getPort(4, 2), atMost(1)).enqueue(captor.capture());
        verify(topology.getPort(4, 3), atMost(1)).enqueue(captor.capture());
        assertFalse(captor.getValue().passedValiant()); // Go valiant
        verify(layer4, times(0)).receive(packet);

    }

    @Test
    public void testValiantPassAckData() {

        // Packet destined for node 3
        when(packet.getSourceId()).thenReturn(4);
        when(packet.getDestinationId()).thenReturn(3);
        when(packet.isACK()).thenReturn(true);
        when(packet.getAcknowledgementNumber()).thenReturn(133L);

        // Node 4 should receive it, and forward directly, as it is an ACK
        device4identity.receiveFromIntermediary(packet);
        ArgumentCaptor<ValiantEncapsulation> captor = ArgumentCaptor.forClass(ValiantEncapsulation.class);
        verify(topology.getPort(4, 2), times(0)).enqueue(captor.capture());
        verify(topology.getPort(4, 3), times(1)).enqueue(captor.capture());
        assertTrue(captor.getValue().passedValiant()); // Direct forward
        verify(layer4, times(0)).receive(packet);

    }

    @Test
    public void testValiantPassSyn() {

        // Packet destined for node 3
        when(packet.getSourceId()).thenReturn(4);
        when(packet.getDestinationId()).thenReturn(3);
        when(packet.isSYN()).thenReturn(true);

        // Node 4 should receive it, and forward directly, as it is an ACK
        device4identity.receiveFromIntermediary(packet);
        ArgumentCaptor<ValiantEncapsulation> captor = ArgumentCaptor.forClass(ValiantEncapsulation.class);
        verify(topology.getPort(4, 2), times(0)).enqueue(captor.capture());
        verify(topology.getPort(4, 3), times(1)).enqueue(captor.capture());
        assertTrue(captor.getValue().passedValiant()); // Direct forward
        verify(layer4, times(0)).receive(packet);

    }

    @Test
    public void testValiantPassAckThirdOfHandshake() {

        // Packet destined for node 3
        when(packet.getSourceId()).thenReturn(4);
        when(packet.getDestinationId()).thenReturn(3);
        when(packet.isACK()).thenReturn(true);
        when(packet.getAcknowledgementNumber()).thenReturn(1L);

        // Node 4 should receive it, and forward it valiantly, as it is the third ACK of the handshake and it should preceed the initial window
        device4identity.receiveFromIntermediary(packet);
        ArgumentCaptor<ValiantEncapsulation> captor = ArgumentCaptor.forClass(ValiantEncapsulation.class);
        verify(topology.getPort(4, 2), atMost(1)).enqueue(captor.capture());
        verify(topology.getPort(4, 3), atMost(1)).enqueue(captor.capture());
        assertFalse(captor.getValue().passedValiant()); // Via valiant
        verify(layer4, times(0)).receive(packet);

    }

    @Test
    public void testValiantPassEnPassant() {

        // Packet destined for node 4
        when(packet.getDestinationId()).thenReturn(4);

        // Create encapsulation for packet to valiant node 3
        ValiantEncapsulation encapsulation = new ValiantEncapsulation(packet, 3);

        // Node 4 should receive it, and decapsulate it, forward to real destination
        device4identity.receive(encapsulation);
        verify(topology.getPort(4, 2), times(0)).enqueue(encapsulation);
        verify(topology.getPort(4, 3), times(0)).enqueue(encapsulation);
        assertFalse(encapsulation.passedValiant()); // did not pass valiant, but is at the true destination
        verify(layer4, times(1)).receive(packet);

    }

    @Test
    public void testValiantSelectionFlowId() {

        // Counter of how many times each valiant node candidate is chosen
        List<Integer> counter = new ArrayList<>();
        counter.add(0);
        counter.add(0);
        counter.add(0);
        counter.add(0);
        counter.add(0);

        for (int fid = 0; fid < 1000; fid += 1) {

            // Create packet with distinguishable random hash
            TcpPacket packetLocal = new FullExtTcpPacket(fid, 0, 4, 1, 100, 3525, 255, 333, 552, false, false, false, false, false, false, false, false, false, 0, 0);

            // Pass packet to device
            device4identity.receiveFromTransportLayer(packetLocal);

            // Check which port was used and what valiant node was selected
            ArgumentCaptor<ValiantEncapsulation> captor = ArgumentCaptor.forClass(ValiantEncapsulation.class);
            verify(topology.getPort(4, 2), atMost(1)).enqueue(captor.capture());
            verify(topology.getPort(4, 3), atMost(1)).enqueue(captor.capture());
            reset(topology.getPort(4, 2));
            reset(topology.getPort(4, 3));
            assertEquals(1, captor.getAllValues().size());

            // Store how often the valiant node is used
            counter.set(captor.getValue().getValiantDestination(), counter.get(captor.getValue().getValiantDestination()) + 1);

        }

        // Node 4 (source) and node 1 (destination) cannot be valiant nodes
        assertEquals(0, (int) counter.get(1));
        assertEquals(0, (int) counter.get(4));

        // Print actual distribution of valiant nodes
        for (int i = 0; i < 5; i++) {
            if (counter.get(i) < 166 && i != 1 && i != 4) {
                throw new RuntimeException("Extremely poor hashing; only half of expectation achieved for significant sample size.");
            }
            System.out.println(i + ": " + (counter.get(i) / 1000.0));
        }

    }

    @Test
    public void testSameValiantSelected() {
        for (int fid = 0; fid < 99; fid++) {

            // Two packets with the same flow identifier
            TcpPacket packetLocal = new FullExtTcpPacket(fid, 0, 4, 1, 100, 3525, 255, 333, 552, false, false, false, false, false, false, false, false, false, 0, 0);
            TcpPacket packetLocal2 = new FullExtTcpPacket(fid, 0, 4, 1, 100, 3525, 255, 333, 552, false, false, false, false, false, false, false, false, false, 0, 0);

            // Pass both packets to the device
            device4identity.receiveFromTransportLayer(packetLocal);
            device4identity.receiveFromTransportLayer(packetLocal2);

            // Both must have taken the same route
            ArgumentCaptor<ValiantEncapsulation> captorA = ArgumentCaptor.forClass(ValiantEncapsulation.class);
            ArgumentCaptor<ValiantEncapsulation> captorB = ArgumentCaptor.forClass(ValiantEncapsulation.class);
            verify(topology.getPort(4, 2), atMost(2)).enqueue(captorA.capture());
            verify(topology.getPort(4, 3), atMost(2)).enqueue(captorB.capture());
            reset(topology.getPort(4, 2));
            reset(topology.getPort(4, 3));

            // Both packets must have gone to exactly the same valiant node and taken the same ECMP route
            assertTrue(captorA.getAllValues().size() == 2 || captorB.getAllValues().size() == 2);
            if (captorA.getAllValues().size() == 2) {
                assertEquals(captorA.getAllValues().get(0).getValiantDestination(), captorA.getAllValues().get(1).getValiantDestination());
            } else {
                assertEquals(captorB.getAllValues().get(0).getValiantDestination(), captorB.getAllValues().get(1).getValiantDestination());
            }

        }

    }

    @Test
    public void testToString() {
        System.out.println(device4identity.toString());
    }

}
