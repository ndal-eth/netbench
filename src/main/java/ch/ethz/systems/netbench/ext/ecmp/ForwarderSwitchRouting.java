package ch.ethz.systems.netbench.ext.ecmp;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.routing.RoutingPopulator;

import java.util.Map;

public class ForwarderSwitchRouting extends RoutingPopulator {

    private final Map<Integer, NetworkDevice> idToNetworkDevice;

    public ForwarderSwitchRouting(Map<Integer, NetworkDevice> idToNetworkDevice) {
        this.idToNetworkDevice = idToNetworkDevice;
        SimulationLogger.logInfo("Routing", "SINGLE_FORWARD");
    }

    /**
     * Initialize the single-forward routing tables in the network devices.
    */
    @Override
    public void populateRoutingTables() {
        EcmpRoutingUtility.populateShortestPathRoutingTables(idToNetworkDevice, false);
    }

}
