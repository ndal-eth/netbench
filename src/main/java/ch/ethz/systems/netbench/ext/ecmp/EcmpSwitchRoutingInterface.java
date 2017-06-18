package ch.ethz.systems.netbench.ext.ecmp;

import ch.ethz.systems.netbench.core.network.OutputPort;

public interface EcmpSwitchRoutingInterface {

    /**
     * Add another hop opportunity to the routing table for the given destination.
     *
     * @param destinationId     Destination identifier
     * @param nextHopId         A network device identifier where it could go to next (must have already been added
     *                          as connection}, else will throw an illegal
     *                          argument exception.
     */
    void addDestinationToNextSwitch(int destinationId, int nextHopId);

}
