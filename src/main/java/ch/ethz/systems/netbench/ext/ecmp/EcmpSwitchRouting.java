package ch.ethz.systems.netbench.ext.ecmp;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.routing.RoutingPopulator;

import java.util.Map;

public class EcmpSwitchRouting extends RoutingPopulator {

    private final Map<Integer, NetworkDevice> idToNetworkDevice;

    public EcmpSwitchRouting(Map<Integer, NetworkDevice> idToNetworkDevice) {
        this.idToNetworkDevice = idToNetworkDevice;
        SimulationLogger.logInfo("Routing", "ECMP");
    }

    /**
     * Initialize the multi-forwarding routing tables in the network devices.
     */
    @Override
    public void populateRoutingTables() {
        EcmpRoutingUtility.populateShortestPathRoutingTables(idToNetworkDevice, true);
    }

}
