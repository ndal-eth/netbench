package ch.ethz.systems.netbench.xpt.sourcerouting;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.config.TopologyServerExtender;
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

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SourceRoutingSwitchExtendedTest {

    /*
     * Topology:
     *     5   6
     *     |   |
     * 4---0---1---7
     *     |   |
     *     |   |
     * 8---2---3---11
     *     |   |
     *     |   |
     *     9   10
     *
     * 4 and 5 are the "servers" of 0, 6 and 7 are the "servers" of 1 etc. etc.
     */
    private TestTopologyPortsConstruction topology;

    @Mock
    private TcpPacket packet;

    private File tempTopologyFile;

    @Before
    public void setup() throws IOException {

        Simulator.setup(0, new NBProperties(BaseAllowedProperties.PROPERTIES_RUN, BaseAllowedProperties.LOG));
        tempTopologyFile = File.createTempFile("temp-topology", ".tmp");

        TopologyServerExtender extender = new TopologyServerExtender(
                "example/topologies/simple/simple_n4_v2.topology",
                tempTopologyFile.getAbsolutePath()
        );
        extender.extendRegular(2);
        Simulator.getConfiguration().overrideProperty("scenario_topology_file", tempTopologyFile.getAbsolutePath());

        topology = new TestTopologyPortsConstruction(
                "0-1,1-3,3-2,2-0,4-0,5-0,6-1,7-1,8-2,9-2,10-3,11-3"
        );

    }

    @After
    public void cleanup() {
        Simulator.reset();
        assertTrue(tempTopologyFile.delete());
    }

    @Test
    public void testWithinSameToR() {

        // Create device with ports
        SourceRoutingSwitch device = new SourceRoutingSwitch(4, null, 12, new IdentityFlowletIntermediary());
        device.addConnection(topology.getPort(4, 0));

        // Packet from 4->5
        when(packet.getSourceId()).thenReturn(4);
        when(packet.getDestinationId()).thenReturn(5);

        ArgumentCaptor<SourceRoutingEncapsulation> captor = ArgumentCaptor.forClass(SourceRoutingEncapsulation.class);

        // Give device the packet
        device.receiveFromTransportLayer(packet);

        // Ensure it goes to 0
        verify(topology.getPort(4, 0), times(1)).enqueue(captor.capture());

        // Make sure the encapsulation is correct
        SourceRoutingEncapsulation encapsulation = captor.getValue();
        assertTrue(packet == encapsulation.getPacket());
        assertEquals(packet.getDestinationId(), encapsulation.getDestinationId());

        // Next hop must be 5
        assertEquals(5, encapsulation.nextHop());

    }

    @Test
    public void testCrossToR() {

        SourceRoutingSwitch deviceToR = new SourceRoutingSwitch(0, null, 12, new IdentityFlowletIntermediary());
        deviceToR.addConnection(topology.getPort(0, 1));
        deviceToR.addConnection(topology.getPort(0, 2));
        deviceToR.addConnection(topology.getPort(0, 4));
        deviceToR.addConnection(topology.getPort(0, 5));
        deviceToR.addPathToDestination(3, makePath(new Integer[]{0,1,3}));

        // Create device with ports
        SourceRoutingSwitch device = new SourceRoutingSwitch(4, null, 12, new IdentityFlowletIntermediary());
        device.addConnection(topology.getPort(4, 0));

        when(topology.getPort(4, 0).getTargetDevice()).thenReturn(deviceToR);

        // Packet from 4->11
        when(packet.getSourceId()).thenReturn(4);
        when(packet.getDestinationId()).thenReturn(11);

        ArgumentCaptor<SourceRoutingEncapsulation> captor = ArgumentCaptor.forClass(SourceRoutingEncapsulation.class);

        // Give device the packet
        device.receiveFromTransportLayer(packet);

        // Ensure it goes to 0
        verify(topology.getPort(4, 0), times(1)).enqueue(captor.capture());

        // Make sure the encapsulation is correct
        SourceRoutingEncapsulation encapsulation = captor.getValue();
        assertTrue(packet == encapsulation.getPacket());
        assertEquals(packet.getDestinationId(), encapsulation.getDestinationId());

        // Next hop must be 1-3-11
        assertEquals(1, encapsulation.nextHop());
        assertEquals(3, encapsulation.nextHop());
        assertEquals(11, encapsulation.nextHop());

    }

    private SourceRoutingPath makePath(Integer[] path) {
        SourceRoutingPath p = new SourceRoutingPath();
        Collections.addAll(p, path);
        return p;
    }

}