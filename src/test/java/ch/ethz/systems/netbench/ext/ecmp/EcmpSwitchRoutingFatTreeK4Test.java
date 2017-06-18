package ch.ethz.systems.netbench.ext.ecmp;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.RoutingSelector;
import ch.ethz.systems.netbench.core.run.routing.RoutingPopulator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class EcmpSwitchRoutingFatTreeK4Test {

    /*
     * Topology (fat-tree k=4):
     *
                   16   17    18   19
                 //// ////    \\\\  \\\\

         //  //    ||   //    \\   ||    \\   \\
         8   9     10   11    12   13    14   15
         | X |     |  X |     |  X |     |  X |
         0   1     2    3     4    5     6    7

    */

    private EcmpSwitch[] switches = new EcmpSwitch[20];

    @Before
    public void setup() {
        for (int i = 0; i < 20; i++) {
            switches[i] = mock(EcmpSwitch.class);
        }
    }

    @Test
    public void testEcmpFatTreeK4() throws IOException {

        // Create temporary run configuration file
        File tempRunConfig = File.createTempFile("temp-run-config", ".tmp");
        BufferedWriter runConfigWriter = new BufferedWriter(new FileWriter(tempRunConfig));
        runConfigWriter.write("network_device_routing=ecmp\nscenario_topology_file=example/topologies/fat_tree/fat_tree_k4.topology");
        runConfigWriter.close();

        // Setup simulator
        Simulator.setup(1, new NBProperties(tempRunConfig.getAbsolutePath(), BaseAllowedProperties.LOG, BaseAllowedProperties.PROPERTIES_RUN));

        // Mock network devices
        Map<Integer, NetworkDevice> idToNetworkDevice  = new HashMap<>();
        for (int i = 0; i < 20; i++) {
            idToNetworkDevice.put(i, switches[i]);
        }

        // Perform routing
        RoutingPopulator populator = RoutingSelector.selectPopulator(idToNetworkDevice);
        populator.populateRoutingTables();

        // Every bottom node requires two possible next hops to its aggregation node to reach any other bottom node
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (i != j) {
                    verify(switches[i], times(1)).addDestinationToNextSwitch(j, (i % 2 == 0 ? i : i - 1) + 8);
                    verify(switches[i], times(1)).addDestinationToNextSwitch(j, (i % 2 == 0 ? i : i - 1) + 9);
                }
            }
        }

        // Every aggregation node requires two possible next hops to each bottom node not in its own pod
        for (int fromPod = 0; fromPod < 4; fromPod++) {

            for (int toPod = 0; toPod < 4; toPod++) {

                if (fromPod != toPod) {

                    // The two aggregation nodes in the pod
                    int agg1 = 8 + fromPod * 2;
                    int agg2 = 8 + fromPod * 2 + 1;

                    // Go over every bottom node
                    for (int i = toPod * 2; i < toPod * 2 + 1; i++) {

                        // The left aggregator must give two options to 16 or 17
                        verify(switches[agg1], times(1)).addDestinationToNextSwitch(i, 16);
                        verify(switches[agg1], times(1)).addDestinationToNextSwitch(i, 17);

                        // The right aggregator must give two options to 18 or 19
                        verify(switches[agg2], times(1)).addDestinationToNextSwitch(i, 18);
                        verify(switches[agg2], times(1)).addDestinationToNextSwitch(i, 19);

                    }

                }

            }

        }

        // Every aggregation node has one hop to each of the two bottom nodes in its own pod
        for (int i = 8; i < 16; i++) {
            if (i % 2 == 0) {
                verify(switches[i], times(1)).addDestinationToNextSwitch(i - 8, i - 8);
                verify(switches[i], times(1)).addDestinationToNextSwitch(i - 8 + 1, i - 8 + 1);
            } else {
                verify(switches[i], times(1)).addDestinationToNextSwitch(i - 9, i - 9);
                verify(switches[i], times(1)).addDestinationToNextSwitch(i - 9 + 1, i - 9 + 1);
            }
        }

        // Every core nodes requires one hop to each bottom node (one way down)

        // Switch 16
        verify(switches[16], times(1)).addDestinationToNextSwitch(0, 8);
        verify(switches[16], times(1)).addDestinationToNextSwitch(1, 8);
        verify(switches[16], times(1)).addDestinationToNextSwitch(2, 10);
        verify(switches[16], times(1)).addDestinationToNextSwitch(3, 10);
        verify(switches[16], times(1)).addDestinationToNextSwitch(4, 12);
        verify(switches[16], times(1)).addDestinationToNextSwitch(5, 12);
        verify(switches[16], times(1)).addDestinationToNextSwitch(6, 14);
        verify(switches[16], times(1)).addDestinationToNextSwitch(7, 14);

        // Switch 17
        verify(switches[17], times(1)).addDestinationToNextSwitch(0, 8);
        verify(switches[17], times(1)).addDestinationToNextSwitch(1, 8);
        verify(switches[17], times(1)).addDestinationToNextSwitch(2, 10);
        verify(switches[17], times(1)).addDestinationToNextSwitch(3, 10);
        verify(switches[17], times(1)).addDestinationToNextSwitch(4, 12);
        verify(switches[17], times(1)).addDestinationToNextSwitch(5, 12);
        verify(switches[17], times(1)).addDestinationToNextSwitch(6, 14);
        verify(switches[17], times(1)).addDestinationToNextSwitch(7, 14);

        // Switch 18
        verify(switches[18], times(1)).addDestinationToNextSwitch(0, 9);
        verify(switches[18], times(1)).addDestinationToNextSwitch(1, 9);
        verify(switches[18], times(1)).addDestinationToNextSwitch(2, 11);
        verify(switches[18], times(1)).addDestinationToNextSwitch(3, 11);
        verify(switches[18], times(1)).addDestinationToNextSwitch(4, 13);
        verify(switches[18], times(1)).addDestinationToNextSwitch(5, 13);
        verify(switches[18], times(1)).addDestinationToNextSwitch(6, 15);
        verify(switches[18], times(1)).addDestinationToNextSwitch(7, 15);

        // Switch 19
        verify(switches[19], times(1)).addDestinationToNextSwitch(0, 9);
        verify(switches[19], times(1)).addDestinationToNextSwitch(1, 9);
        verify(switches[19], times(1)).addDestinationToNextSwitch(2, 11);
        verify(switches[19], times(1)).addDestinationToNextSwitch(3, 11);
        verify(switches[19], times(1)).addDestinationToNextSwitch(4, 13);
        verify(switches[19], times(1)).addDestinationToNextSwitch(5, 13);
        verify(switches[19], times(1)).addDestinationToNextSwitch(6, 15);
        verify(switches[19], times(1)).addDestinationToNextSwitch(7, 15);

        // Reset simulator
        assertTrue(tempRunConfig.delete());
        Simulator.reset();

    }

}
