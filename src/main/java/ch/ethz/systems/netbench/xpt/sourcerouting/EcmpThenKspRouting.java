package ch.ethz.systems.netbench.xpt.sourcerouting;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.run.routing.RoutingPopulator;
import ch.ethz.systems.netbench.ext.ecmp.EcmpRoutingUtility;
import ch.ethz.systems.netbench.ext.ecmp.EcmpSwitchRouting;

import java.util.Map;

public class EcmpThenKspRouting extends RoutingPopulator {

    private final Map<Integer, NetworkDevice> idToNetworkDevice;

    public EcmpThenKspRouting(Map<Integer, NetworkDevice> idToNetworkDevice) {
        this.idToNetworkDevice = idToNetworkDevice;
        SimulationLogger.logInfo("Routing", "ECMP_THEN_SR");
    }


    @Override
    public void populateRoutingTables() {

        // Populate ECMP routing state
        new EcmpSwitchRouting(idToNetworkDevice).populateRoutingTables();

        // Populate source routing (SR) routing state
        new KShortestPathsSwitchRouting(idToNetworkDevice).populateRoutingTables();

    }


}
