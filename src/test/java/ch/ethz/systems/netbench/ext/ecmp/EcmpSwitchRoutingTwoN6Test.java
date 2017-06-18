package ch.ethz.systems.netbench.ext.ecmp;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.RoutingSelector;
import ch.ethz.systems.netbench.core.run.routing.RoutingPopulator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EcmpSwitchRoutingTwoN6Test {

    /*
     * Topology:
     *
     *  0--2--3
     *  | /   |
     *  4    /
     *  | \ /
     *  5--1
    */

    @Mock
    private EcmpSwitch switch0;

    @Mock
    private EcmpSwitch switch1;

    @Mock
    private EcmpSwitch switch2;

    @Mock
    private EcmpSwitch switch3;

    @Mock
    private EcmpSwitch switch4;

    @Mock
    private EcmpSwitch switch5;

    @Test
    public void testEcmpTwoN6() throws IOException {


        // Create temporary run configuration file
        File tempRunConfig = File.createTempFile("temp-run-config", ".tmp");
        BufferedWriter runConfigWriter = new BufferedWriter(new FileWriter(tempRunConfig));
        runConfigWriter.write(
                "network_device_routing=ecmp\n" +
                 "scenario_topology_file=example/topologies/simple/simple_n6_v2.topology"
        );
        runConfigWriter.close();

        // Setup simulator
        Simulator.setup(1, new NBProperties(tempRunConfig.getAbsolutePath(), BaseAllowedProperties.PROPERTIES_RUN, BaseAllowedProperties.LOG));

        // Mock network devices
        Map<Integer, NetworkDevice> idToNetworkDevice  = new HashMap<>();
        idToNetworkDevice.put(0, switch0);
        idToNetworkDevice.put(1, switch1);
        idToNetworkDevice.put(2, switch2);
        idToNetworkDevice.put(3, switch3);
        idToNetworkDevice.put(4, switch4);
        idToNetworkDevice.put(5, switch5);

        // Perform routing
        RoutingPopulator populator = RoutingSelector.selectPopulator(idToNetworkDevice);
        populator.populateRoutingTables();

        // Switch 0
        verify(switch0, times(1)).addDestinationToNextSwitch(1, 4);
        verify(switch0, times(1)).addDestinationToNextSwitch(2, 2);
        verify(switch0, times(1)).addDestinationToNextSwitch(3, 2);
        verify(switch0, times(1)).addDestinationToNextSwitch(4, 4);
        verify(switch0, times(1)).addDestinationToNextSwitch(5, 4);

        // Switch 1
        verify(switch1, times(1)).addDestinationToNextSwitch(0, 4);
        verify(switch1, times(1)).addDestinationToNextSwitch(2, 4);
        verify(switch1, times(1)).addDestinationToNextSwitch(2, 3);
        verify(switch1, times(1)).addDestinationToNextSwitch(3, 3);
        verify(switch1, times(1)).addDestinationToNextSwitch(4, 4);
        verify(switch1, times(1)).addDestinationToNextSwitch(5, 5);

        // Switch 2
        verify(switch2, times(1)).addDestinationToNextSwitch(0, 0);
        verify(switch2, times(1)).addDestinationToNextSwitch(1, 4);
        verify(switch2, times(1)).addDestinationToNextSwitch(1, 3);
        verify(switch2, times(1)).addDestinationToNextSwitch(3, 3);
        verify(switch2, times(1)).addDestinationToNextSwitch(4, 4);
        verify(switch2, times(1)).addDestinationToNextSwitch(5, 4);

        // Switch 3
        verify(switch3, times(1)).addDestinationToNextSwitch(0, 2);
        verify(switch3, times(1)).addDestinationToNextSwitch(1, 1);
        verify(switch3, times(1)).addDestinationToNextSwitch(2, 2);
        verify(switch3, times(1)).addDestinationToNextSwitch(4, 2);
        verify(switch3, times(1)).addDestinationToNextSwitch(4, 1);
        verify(switch3, times(1)).addDestinationToNextSwitch(5, 1);

        // Switch 4
        verify(switch4, times(1)).addDestinationToNextSwitch(0, 0);
        verify(switch4, times(1)).addDestinationToNextSwitch(1, 1);
        verify(switch4, times(1)).addDestinationToNextSwitch(2, 2);
        verify(switch4, times(1)).addDestinationToNextSwitch(3, 1);
        verify(switch4, times(1)).addDestinationToNextSwitch(3, 2);
        verify(switch4, times(1)).addDestinationToNextSwitch(5, 5);

        // Switch 5
        verify(switch5, times(1)).addDestinationToNextSwitch(0, 4);
        verify(switch5, times(1)).addDestinationToNextSwitch(1, 1);
        verify(switch5, times(1)).addDestinationToNextSwitch(2, 4);
        verify(switch5, times(1)).addDestinationToNextSwitch(3, 1);
        verify(switch5, times(1)).addDestinationToNextSwitch(4, 4);

        // There should be no other interactions
        verifyNoMoreInteractions(switch0, switch1, switch2, switch3, switch4, switch5);

        // Reset simulator
        Simulator.reset();
        assertTrue(tempRunConfig.delete());

    }

}
