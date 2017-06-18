package ch.ethz.systems.netbench.ext.ecmp;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.flowlet.IdentityFlowletIntermediary;
import ch.ethz.systems.netbench.testutility.TestTopologyPortsConstruction;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EcmpSwitchTest {

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

    private EcmpSwitch device0;
    private EcmpSwitch device1;
    private EcmpSwitch device2;
    private EcmpSwitch device3;
    private EcmpSwitch device4;

    @Mock private TransportLayer layer0;
    @Mock private TransportLayer layer1;
    @Mock private TransportLayer layer2;
    @Mock private TransportLayer layer3;
    @Mock private TransportLayer layer4;


    @Before
    public void setup() {

        Simulator.setup(0);
        topology = new TestTopologyPortsConstruction(
                "0-1,1-2,2-4,4-3,3-1"
        );

        // ECMP routing rules for device 0
        device0 = new EcmpSwitch(0, layer0, 5, new IdentityFlowletIntermediary());
        device0.addConnection(topology.getPort(0, 1));
        device0.addDestinationToNextSwitch(1, 1);
        device0.addDestinationToNextSwitch(2, 1);
        device0.addDestinationToNextSwitch(3, 1);
        device0.addDestinationToNextSwitch(4, 1);

        // ECMP routing rules for device 1
        device1 = new EcmpSwitch(1, layer1, 5, new IdentityFlowletIntermediary());
        device1.addConnection(topology.getPort(1, 0));
        device1.addConnection(topology.getPort(1, 2));
        device1.addConnection(topology.getPort(1, 3));
        device1.addDestinationToNextSwitch(0, 0);
        device1.addDestinationToNextSwitch(2, 2);
        device1.addDestinationToNextSwitch(3, 3);
        device1.addDestinationToNextSwitch(4, 2);
        device1.addDestinationToNextSwitch(4, 3);

        // ECMP routing rules for device 2
        device2 = new EcmpSwitch(2, layer2, 5, new IdentityFlowletIntermediary());
        device2.addConnection(topology.getPort(2, 1));
        device2.addConnection(topology.getPort(2, 4));
        device2.addDestinationToNextSwitch(0, 1);
        device2.addDestinationToNextSwitch(1, 1);
        device2.addDestinationToNextSwitch(3, 1);
        device2.addDestinationToNextSwitch(3, 4);
        device2.addDestinationToNextSwitch(4, 4);

        // ECMP routing rules for device 3
        device3 = new EcmpSwitch(3, layer3, 5, new IdentityFlowletIntermediary());
        device3.addConnection(topology.getPort(3, 1));
        device3.addConnection(topology.getPort(3, 4));
        device3.addDestinationToNextSwitch(0, 1);
        device3.addDestinationToNextSwitch(1, 1);
        device3.addDestinationToNextSwitch(2, 1);
        device3.addDestinationToNextSwitch(2, 4);
        device3.addDestinationToNextSwitch(4, 4);

        // ECMP routing rules for device 4
        device4 = new EcmpSwitch(4, layer4, 5, new IdentityFlowletIntermediary());
        device4.addConnection(topology.getPort(4, 2));
        device4.addConnection(topology.getPort(4, 3));
        device4.addDestinationToNextSwitch(0, 2);
        device4.addDestinationToNextSwitch(0, 3);
        device4.addDestinationToNextSwitch(1, 2);
        device4.addDestinationToNextSwitch(1, 3);
        device4.addDestinationToNextSwitch(2, 2);
        device4.addDestinationToNextSwitch(3, 3);

    }

    @After
    public void cleanup() {
        Simulator.reset();
    }

    @Test
    public void testConstructionDuplicate() {

        // Create an ECMP switch with port 0 to 1...
        device0 = new EcmpSwitch(0, null, 5, new IdentityFlowletIntermediary());
        device0.addConnection(topology.getPort(0, 1));
        device0.addDestinationToNextSwitch(1, 1);

        // ... and try to add the same destination to switch again
        boolean thrown = false;
        try {
            device0.addDestinationToNextSwitch(1, 1);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

    }

    @Test
    public void testConstructionNonExisting() {

        // Create switch with 1->0, 1->2, and 1->3
        device1 = new EcmpSwitch(1, null, 5, new IdentityFlowletIntermediary());
        device1.addConnection(topology.getPort(1, 0));
        device1.addConnection(topology.getPort(1, 2));
        device1.addConnection(topology.getPort(1, 3));
        device1.addDestinationToNextSwitch(0, 0);

        // ... but deny that you can travel to 2 via a non-existing port
        boolean thrown = false;
        try {
            device1.addDestinationToNextSwitch(2, 4); // there is no port to 4
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void testSingleConsumption() {

        // Give packet to the destination switch, should consume it itself
        // by passing it to the transport layer
        when(packet.getDestinationId()).thenReturn(4);
        device4.receive(packet);
        verify(topology.getPort(4, 2), times(0)).enqueue(packet);
        verify(topology.getPort(4, 3), times(0)).enqueue(packet);
        verify(layer4, times(1)).receive(packet);

    }

    @Test
    public void testSingleConsumptionDirect() {

        // Packet originates from itself and is addressed to itself
        when(packet.getDestinationId()).thenReturn(4);
        device4.receiveFromTransportLayer(packet);
        verify(topology.getPort(4, 2), times(0)).enqueue(packet);
        verify(topology.getPort(4, 3), times(0)).enqueue(packet);
        verify(layer4, times(1)).receive(packet);

    }

    @Test
    public void testSingleForward() {

        // Send packet from 2 to 4, there is one option
        when(packet.getDestinationId()).thenReturn(4);
        device2.receive(packet);
        verify(topology.getPort(2, 1), times(0)).enqueue(packet);
        verify(topology.getPort(2, 4), times(1)).enqueue(packet);

    }

    @Test
    public void testSingleForwardTwoOptions() {

        for (int i = 0; i < 2; i++) {
            when(packet.getHash(any(Integer.class))).thenReturn(i);

            ArgumentCaptor<Packet> captor = ArgumentCaptor.forClass(Packet.class);

            // Send packet from 1 to 4, there are two options
            when(packet.getDestinationId()).thenReturn(4);

            device1.receive(packet);
            verify(topology.getPort(1, 2), atMost((i + 1) % 2)).enqueue(captor.capture());
            verify(topology.getPort(1, 3), atMost(i % 2)).enqueue(captor.capture());
            reset(topology.getPort(1, 2), topology.getPort(1, 3));

            // Only one decision can have been made
            assertEquals(1, captor.getAllValues().size());

        }

    }

    @Test
    public void testSingleForwardFromTransportLayer() {
        when(packet.getDestinationId()).thenReturn(1);
        device3.receiveFromTransportLayer(packet);
        verify(topology.getPort(3, 1), times(1)).enqueue(packet);
        verify(topology.getPort(3, 4), times(0)).enqueue(packet);
    }

    @Test
    public void testToString() {
        assertNotNull(device4.toString());
    }

}
