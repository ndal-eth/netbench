package ch.ethz.systems.netbench.ext.ecmp;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.flowlet.IdentityFlowletIntermediary;
import ch.ethz.systems.netbench.testutility.TestTopologyPortsConstruction;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ForwarderSwitchTest {

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


    @Before
    public void setup() {
        Simulator.setup(0);
        topology = new TestTopologyPortsConstruction(
                "0-1,1-2,2-4,4-3,3-1"
        );
    }

    @After
    public void cleanup() {
        Simulator.reset();
    }

    @Test
    public void testTLChecks() {

        // Forward switch with one output port
        ForwarderSwitch device = new ForwarderSwitch(0, null, 5, new IdentityFlowletIntermediary());
        assert(!device.isServer());

        TransportLayer layer = mock(TransportLayer.class);
        ForwarderSwitch device2 = new ForwarderSwitch(33, layer, 5, new IdentityFlowletIntermediary());
        assert(device2.isServer());
        assertEquals(layer, device2.getTransportLayer());

    }

    @Test
    public void testSingleForward() {

        // Forward switch with one output port
        ForwarderSwitch device = new ForwarderSwitch(0, null, 5, new IdentityFlowletIntermediary());
        device.addConnection(topology.getPort(0, 1));
        device.setDestinationToNextSwitch(1, 1);
        device.setDestinationToNextSwitch(2, 1);
        device.setDestinationToNextSwitch(3, 1);
        device.setDestinationToNextSwitch(4, 1);
        assertTrue(device.hasConnection(1));
        assertFalse(device.hasConnection(2));


        // Packet wants to go to 4
        when(packet.getDestinationId()).thenReturn(4);

        device.receive(packet);

        // So it must choose port 0 to 1 by default
        verify(topology.getPort(0, 1), times(1)).enqueue(packet);

    }

    @Test
    public void testSingleForwardTwoOptions() {

        // Forward switch with one output port
        ForwarderSwitch device = new ForwarderSwitch(4, null, 5, new IdentityFlowletIntermediary());
        device.addConnection(topology.getPort(4, 2));
        device.addConnection(topology.getPort(4, 3));
        device.setDestinationToNextSwitch(0, 2);
        device.setDestinationToNextSwitch(1, 2);
        device.setDestinationToNextSwitch(2, 2);
        device.setDestinationToNextSwitch(3, 3);

        // Packet wants to go to 0
        when(packet.getDestinationId()).thenReturn(0);
        device.receive(packet);

        // So it must choose port 0 to 2 as it is set as next destination to switch
        verify(topology.getPort(4, 2), times(1)).enqueue(packet);
        verify(topology.getPort(4, 3), times(0)).enqueue(packet);

    }

    @Test
    public void testSingleForwardFromTransportLayer() {

        // Forward switch with one output port
        ForwarderSwitch device = new ForwarderSwitch(0, null, 5, new IdentityFlowletIntermediary());
        device.addConnection(topology.getPort(0, 1));
        device.setDestinationToNextSwitch(1, 1);
        device.setDestinationToNextSwitch(2, 1);
        device.setDestinationToNextSwitch(3, 1);
        device.setDestinationToNextSwitch(4, 1);

        // Packet to 4 goes over 0-1
        when(packet.getDestinationId()).thenReturn(4);
        device.receiveFromTransportLayer(packet);
        verify(topology.getPort(0, 1), times(1)).enqueue(packet);

    }

    @Test
    public void testInstNonExistingPort() {
        boolean thrown = false;
        try {
            ForwarderSwitch device = new ForwarderSwitch(0, null, 5, new IdentityFlowletIntermediary());
            device.addConnection(topology.getPort(0, 1));
            device.setDestinationToNextSwitch(1, 2);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void testAddConnectionInvalidPortInstance() {

        // Illegal origin
        boolean thrown = false;
        try {
            ForwarderSwitch device = new ForwarderSwitch(0, null, 5, new IdentityFlowletIntermediary());
            device.addConnection(topology.getPort(1, 2));
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

        // Already exists
        thrown = false;
        try {
            ForwarderSwitch device = new ForwarderSwitch(0, null, 5, new IdentityFlowletIntermediary());
            device.addConnection(topology.getPort(0, 1));
            device.addConnection(topology.getPort(0, 1));
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

    }

    @Test
    public void testGetIdentifier() {
        ForwarderSwitch device = new ForwarderSwitch(3, null, 5, new IdentityFlowletIntermediary());
        assertEquals(3, device.getIdentifier());
    }

    @Test
    public void testSingleConsumption() {

        // Device 3 with ports to 1 and 4
        TransportLayer layer = mock(TransportLayer.class);
        ForwarderSwitch device = new ForwarderSwitch(3, layer, 5, new IdentityFlowletIntermediary());
        device.addConnection(topology.getPort(3, 1));
        device.addConnection(topology.getPort(3, 4));
        device.setDestinationToNextSwitch(0, 1);
        device.setDestinationToNextSwitch(1, 1);
        device.setDestinationToNextSwitch(2, 4);
        device.setDestinationToNextSwitch(4, 4);

        // Reception of a packet to itself
        when(packet.getDestinationId()).thenReturn(3);
        device.receive(packet);
        verify(topology.getPort(3, 1), times(0)).enqueue(packet);
        verify(topology.getPort(3, 4), times(0)).enqueue(packet);
        verify(layer, times(1)).receive(packet);

    }

    @Test
    public void testSingleConsumptionDirect() {

        // Device 3 with ports to 1 and 4
        TransportLayer layer = mock(TransportLayer.class);
        ForwarderSwitch device = new ForwarderSwitch(3, layer, 5, new IdentityFlowletIntermediary());
        device.addConnection(topology.getPort(3, 1));
        device.addConnection(topology.getPort(3, 4));
        device.setDestinationToNextSwitch(0, 1);
        device.setDestinationToNextSwitch(1, 1);
        device.setDestinationToNextSwitch(2, 4);
        device.setDestinationToNextSwitch(4, 4);

        // Immediately receive packet from itself to itself
        when(packet.getDestinationId()).thenReturn(3);
        device.receiveFromTransportLayer(packet);
        verify(topology.getPort(3, 1), times(0)).enqueue(packet);
        verify(topology.getPort(3, 4), times(0)).enqueue(packet);
        verify(layer, times(1)).receive(packet);

    }

    @Test
    public void testToString() {
        ForwarderSwitch device = new ForwarderSwitch(3, null, 5, new IdentityFlowletIntermediary());
        device.addConnection(topology.getPort(3, 1));
        device.addConnection(topology.getPort(3, 4));
        device.setDestinationToNextSwitch(0, 1);
        device.setDestinationToNextSwitch(1, 1);
        device.setDestinationToNextSwitch(2, 4);
        device.setDestinationToNextSwitch(4, 4);
        assertNotNull(device.toString());
    }

}
