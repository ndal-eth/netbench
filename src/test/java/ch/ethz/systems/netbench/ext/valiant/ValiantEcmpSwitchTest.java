package ch.ethz.systems.netbench.ext.valiant;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
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

import java.io.IOException;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ValiantEcmpSwitchTest {

    /*
     * Topology:
     *     0---1        4 is 0's "server" (as 0 is a ToR)
     *     |   |
     *     |   |
     *     2---3
     */
    private TestTopologyPortsConstruction topology;

    @Mock
    private TcpPacket packet;

    private RangeValiantSwitch device0identity;

    @Mock
    private TransportLayer layer1;


    @Before
    public void setup() throws IOException {


        // Setup simulator
        Simulator.setup(0, new NBProperties(BaseAllowedProperties.LOG, BaseAllowedProperties.PROPERTIES_RUN));

        // Set properties
        Simulator.getConfiguration().overrideProperty("scenario_topology_extend_with_servers", "true");
        Simulator.getConfiguration().overrideProperty("scenario_topology_file", "example/topologies/simple/simple_n4_v2.topology");
        Simulator.getConfiguration().overrideProperty("scenario_topology_extend_servers_per_tl_node", "2");

        // Create port topology
        topology = new TestTopologyPortsConstruction(
                "0-1,1-3,3-2,0-2,0-4,0-5,1-6,1-7,2-8,2-9,3-10,3-11"
        );

        // Initialize ECMP routing scheme for device number 1 with identity flowlet intermediary
        device0identity = new RangeValiantSwitch(0, layer1, 12, new IdentityFlowletIntermediary(), 0, 3);
        device0identity.addConnection(topology.getPort(0, 1));
        device0identity.addConnection(topology.getPort(0, 2));
        device0identity.addConnection(topology.getPort(0, 4));
        device0identity.addConnection(topology.getPort(0, 5));
        device0identity.addDestinationToNextSwitch(1, 1);
        device0identity.addDestinationToNextSwitch(2, 2);
        device0identity.addDestinationToNextSwitch(3, 1);
        device0identity.addDestinationToNextSwitch(3, 2);
        device0identity.addDestinationToNextSwitch(4, 4);
        device0identity.addDestinationToNextSwitch(5, 5);
        device0identity.addDestinationToNextSwitch(6, 1);
        device0identity.addDestinationToNextSwitch(7, 1);
        device0identity.addDestinationToNextSwitch(8, 2);
        device0identity.addDestinationToNextSwitch(9, 2);
        device0identity.addDestinationToNextSwitch(10, 1);
        device0identity.addDestinationToNextSwitch(10, 2);
        device0identity.addDestinationToNextSwitch(11, 1);
        device0identity.addDestinationToNextSwitch(11, 2);


    }

    @After
    public void cleanup() {
        Simulator.reset();
    }

    @Test
    public void testValiantPassEnPassantWithServerExtension() {

        // Packet destined for node 4
        when(packet.getDestinationId()).thenReturn(4);

        // Create encapsulation for packet to 4 that is on its way to 2 for VLB reasons
        ValiantEncapsulation encapsulation = new ValiantEncapsulation(packet, 2);

        // Node 1 should receive it, and forward it to 0, the real destination
        device0identity.receive(encapsulation);
        verify(topology.getPort(0, 1), times(0)).enqueue(encapsulation);
        verify(topology.getPort(0, 2), times(0)).enqueue(encapsulation);
        verify(topology.getPort(0, 4), times(1)).enqueue(encapsulation);
        verify(topology.getPort(0, 5), times(0)).enqueue(encapsulation);
        verify(layer1, times(0)).receive(packet);

    }

}
