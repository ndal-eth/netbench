package ch.ethz.systems.netbench.xpt.sourcerouting;

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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SourceRoutingSwitchTest {

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
                "0-1,1-2,2-4,4-3,3-1"
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
        SourceRoutingSwitch device = new SourceRoutingSwitch(1, null, 5, new IdentityFlowletIntermediary());
        device.addConnection(topology.getPort(1, 0));
        device.addConnection(topology.getPort(1, 2));
        device.addConnection(topology.getPort(1, 3));

        // Add path to a certain destination
        SourceRoutingPath path = new SourceRoutingPath();
        path.add(1);
        path.add(3);
        path.add(4);
        device.addPathToDestination(4, path);

        // Initialize packet for that destination
        when(packet.getDestinationId()).thenReturn(4);

        ArgumentCaptor<SourceRoutingEncapsulation> captor = ArgumentCaptor.forClass(SourceRoutingEncapsulation.class);

        // Give device the packet
        device.receiveFromTransportLayer(packet);
        verify(topology.getPort(1, 0), times(0)).enqueue(captor.capture());
        verify(topology.getPort(1, 2), times(0)).enqueue(captor.capture());
        verify(topology.getPort(1, 3), times(1)).enqueue(captor.capture());

        // Make sure the encapsulation is correct
        SourceRoutingEncapsulation encapsulation = captor.getValue();
        assertTrue(packet == encapsulation.getPacket());
        assertEquals(packet.getDestinationId(), encapsulation.getDestinationId());

    }

    @Test
    public void testPassToTransportLayer() {

        TransportLayer transportLayer = mock(TransportLayer.class);

        // Create device 4 with ports 4->2 and 4->3
        SourceRoutingSwitch device = new SourceRoutingSwitch(4, transportLayer, 5, new IdentityFlowletIntermediary());
        device.addConnection(topology.getPort(4, 2));
        device.addConnection(topology.getPort(4, 3));

        // Add path from 1 to 4
        SourceRoutingPath path = new SourceRoutingPath();
        path.add(1);
        path.add(3);
        path.add(4);

        // Create encapsulation and hop it two times (such that it "arrives" at 4)
        when(packet.getDestinationId()).thenReturn(4);
        SourceRoutingEncapsulation encapsulation = new SourceRoutingEncapsulation(packet, path);
        encapsulation.nextHop();
        encapsulation.nextHop();

        // Give it to the network device at 4
        device.receive(encapsulation);

        // Assert that it receives the packet itself
        verify(transportLayer, times(1)).receive(packet);
        verify(topology.getPort(4, 2), times(0)).enqueue(encapsulation);
        verify(topology.getPort(4, 3), times(0)).enqueue(encapsulation);

    }

    @Test
    public void testAddPathToDestination() {

        // Create device with ports
        SourceRoutingSwitch device = new SourceRoutingSwitch(1, null, 5, new IdentityFlowletIntermediary());
        device.addConnection(topology.getPort(1, 0));
        device.addConnection(topology.getPort(1, 2));
        device.addConnection(topology.getPort(1, 3));

        // Correct addition
        device.addPathToDestination(4, makePath(new Integer[]{1, 2, 4}));

        // Duplicate path
        boolean thrown = false;
        try {
            device.addPathToDestination(4, makePath(new Integer[]{1, 2, 4}));
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

        // Empty path
        thrown = false;
        try {
            device.addPathToDestination(4, makePath(new Integer[]{}));
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

        // Non-existing first hop
        thrown = false;
        try {
            device.addPathToDestination(4, makePath(new Integer[]{1, 4, 4}));
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

        // Source incorrect
        thrown = false;
        try {
            device.addPathToDestination(4, makePath(new Integer[]{4, 2, 4}));
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

        // Destination incorrect
        thrown = false;
        try {
            device.addPathToDestination(4, makePath(new Integer[]{1, 2, 1}));
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

        // To itself (I)
        thrown = false;
        try {
            device.addPathToDestination(1, makePath(new Integer[]{1, 2, 1}));
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

        // To itself
        thrown = false;
        try {
            device.addPathToDestination(1, makePath(new Integer[]{1, 2, 1}));
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

    }

    @Test
    public void testAllowingDuplicatePaths() {

        // Create device with ports
        SourceRoutingSwitch device = new SourceRoutingSwitch(1, null, 5, new IdentityFlowletIntermediary());
        device.addConnection(topology.getPort(1, 0));
        device.addConnection(topology.getPort(1, 2));
        device.addConnection(topology.getPort(1, 3));

        // Correct addition
        device.addPathToDestination(4, makePath(new Integer[]{1, 2, 4}));

        // Duplicate path should be skipped if property is set
        Simulator.getConfiguration().setProperty("allow_source_routing_skip_duplicate_paths", "true");
        device.addPathToDestination(4, makePath(new Integer[]{1, 2, 4}));

        // Duplicate path should FAIL after skip duplicate property disabled
        Simulator.getConfiguration().setProperty("allow_source_routing_skip_duplicate_paths", "false");
        boolean thrown = false;
        try {
            device.addPathToDestination(4, makePath(new Integer[]{1, 2, 4}));
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

        // Duplicate path should be added if property is set
        Simulator.getConfiguration().setProperty("allow_source_routing_add_duplicate_paths", "true");
        device.addPathToDestination(4, makePath(new Integer[]{1, 2, 4}));

        // Duplicate path should FAIL after add duplicate property disabled
        Simulator.getConfiguration().setProperty("allow_source_routing_add_duplicate_paths", "false");
        thrown = false;
        try {
            device.addPathToDestination(4, makePath(new Integer[]{1, 2, 4}));
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);

    }

    @Test
    public void testToString() {

        // Create device with ports
        SourceRoutingSwitch device = new SourceRoutingSwitch(1, null, 5, new IdentityFlowletIntermediary());
        device.addConnection(topology.getPort(1, 0));
        device.addConnection(topology.getPort(1, 2));
        device.addConnection(topology.getPort(1, 3));

        // Correct addition
        device.addPathToDestination(0, makePath(new Integer[]{1, 0}));
        device.addPathToDestination(2, makePath(new Integer[]{1, 2}));
        device.addPathToDestination(3, makePath(new Integer[]{1, 3}));
        device.addPathToDestination(4, makePath(new Integer[]{1, 2, 4}));
        device.addPathToDestination(4, makePath(new Integer[]{1, 3, 4}));

        System.out.println(device.toString());

    }

    private SourceRoutingPath makePath(Integer[] path) {
        SourceRoutingPath p = new SourceRoutingPath();
        Collections.addAll(p, path);
        return p;
    }

}