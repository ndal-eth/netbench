package ch.ethz.systems.netbench.xpt.sourcerouting;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.ext.flowlet.IdentityFlowletIntermediary;
import ch.ethz.systems.netbench.testutility.TestTopologyPortsConstruction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EcmpThenSourceRoutingSwitchTest {

    /*
     * Topology:
     *    1
     *  /   \
     * 0--2--4
     *  \   /
     *    3
     */
    private TestTopologyPortsConstruction topology;

    @Mock
    private TcpPacket packet;

    private File tempRunConfig;

    @Before
    public void setup() throws IOException {

        // Create temporary run configuration file
        tempRunConfig = File.createTempFile("temp-run-config", ".tmp");
        BufferedWriter runConfigWriter = new BufferedWriter(new FileWriter(tempRunConfig));
        runConfigWriter.write("scenario_topology_file=example/topologies/simple/simple_n5.topology");
        runConfigWriter.close();

        Simulator.setup(0, new NBProperties(
                tempRunConfig.getAbsolutePath(),
                BaseAllowedProperties.LOG,
                BaseAllowedProperties.PROPERTIES_RUN,
                BaseAllowedProperties.EXPERIMENTAL
        ));
        topology = new TestTopologyPortsConstruction(
                "0-1,1-4,0-2,2-4,3-0,3-4"
        );
    }

    @After
    public void cleanup() {
        Simulator.reset();
        assertTrue(tempRunConfig.delete());
    }

    @Test
    public void testSingleForward() {

        // Create device with ports
        EcmpThenSourceRoutingSwitch device = new EcmpThenSourceRoutingSwitch(1, null, 5, new IdentityFlowletIntermediary(), 5000);
        device.addConnection(topology.getPort(1, 0));
        device.addConnection(topology.getPort(1, 4));

        // Add path to a certain destination
        SourceRoutingPath path = new SourceRoutingPath();
        path.add(1);
        path.add(4);
        path.add(2);
        path.add(0);
        path.add(3);
        device.addPathToDestination(3, path);

        // Add ECMP next hop
        device.addDestinationToNextSwitch(3, 0);

        // Initialize packet for that destination
        when(packet.getDestinationId()).thenReturn(3);
        when(packet.getDataSizeByte()).thenReturn(1000L);

        // 5 packets are sent via ECMP
        for (int i = 0; i < 5; i++) {

            ArgumentCaptor<Packet> captorA = ArgumentCaptor.forClass(Packet.class);

            // Give device the packet
            device.receiveFromTransportLayer(packet);
            verify(topology.getPort(1, 0), times(1 + i)).enqueue(captorA.capture());
            verify(topology.getPort(1, 4), times(0)).enqueue(captorA.capture());

            // Make sure the packet is of the correct type
            assert(captorA.getValue() instanceof TcpPacket);
            assert(!(captorA.getValue() instanceof SourceRoutingEncapsulation));

        }

        // Now threshold will be exceeded
        ArgumentCaptor<Packet> captorB = ArgumentCaptor.forClass(Packet.class);

        // Give device the packet
        device.receiveFromTransportLayer(packet);
        verify(topology.getPort(1, 0), times(5)).enqueue(captorB.capture());
        verify(topology.getPort(1, 4), times(1)).enqueue(captorB.capture());

        // Make sure the packet is of the correct type
        assert(!(captorB.getValue() instanceof TcpPacket));
        assert(captorB.getValue() instanceof SourceRoutingEncapsulation);

    }

}
