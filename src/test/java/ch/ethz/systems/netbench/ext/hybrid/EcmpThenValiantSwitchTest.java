package ch.ethz.systems.netbench.ext.hybrid;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.flowlet.IdentityFlowletIntermediary;
import ch.ethz.systems.netbench.ext.valiant.ValiantEncapsulation;
import ch.ethz.systems.netbench.testutility.TestTopologyPortsConstruction;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EcmpThenValiantSwitchTest {

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

    private EcmpThenValiantSwitch device2identity;

    @Mock
    private TransportLayer layer1;

    @Before
    public void setup() throws IOException {

        // Setup simulator
        Simulator.setup(0, new NBProperties(BaseAllowedProperties.PROPERTIES_RUN, BaseAllowedProperties.LOG));

        // Create port topology
        topology = new TestTopologyPortsConstruction(
                "0-1,1-2,2-4,4-3,3-1"
        );

        // Initialize ECMP routing scheme for device number 1 with identity flowlet intermediary
        device2identity = new EcmpThenValiantSwitch(2, layer1, 5, new IdentityFlowletIntermediary(), 0, 4, 100000);
        device2identity.addConnection(topology.getPort(2, 1));
        device2identity.addConnection(topology.getPort(2, 4));
        device2identity.addDestinationToNextSwitch(0, 1);
        device2identity.addDestinationToNextSwitch(1, 1);
        device2identity.addDestinationToNextSwitch(3, 1);
        device2identity.addDestinationToNextSwitch(3, 4);
        device2identity.addDestinationToNextSwitch(4, 4);

    }

    @After
    public void cleanup() {
        Simulator.reset();
    }

    @Test
    public void testSwitchThreshold() {

        // Packet destined for node 0
        when(packet.getSourceId()).thenReturn(2);
        when(packet.getDestinationId()).thenReturn(0);
        when(packet.getHash(eq(2), anyInt())).thenReturn(4);
        when(packet.getDataSizeByte()).thenReturn(1000L);

        for (int i = 0; i < 105; i++) {

            // Create encapsulation for packet to 0 that is on its way to 2 for VLB reasons
            device2identity.receiveFromTransportLayer(packet);

        }

        // Node 2 should receive it, and forward it towards 0, the real destination
        verify(topology.getPort(2, 1), times(100)).enqueue(any(ValiantEncapsulation.class));
        verify(topology.getPort(2, 4), times(5)).enqueue(any(ValiantEncapsulation.class));
        verify(layer1, times(0)).receive(packet);

    }

    @Test
    public void testToString() {
        assertNotNull(device2identity.toString());
    }

}
