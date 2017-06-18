package ch.ethz.systems.netbench.xpt.voijslav.ports;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.run.infrastructure.OutputPortGenerator;

public class BoundedPriorityOutputPortGenerator extends OutputPortGenerator {

	private long maxQueueSizeInBits;
	
    public BoundedPriorityOutputPortGenerator(long maxQueueSizeInBits) {
    	this.maxQueueSizeInBits = maxQueueSizeInBits;
        SimulationLogger.logInfo("Port", "BOUNDED_PRIORITY_PORT(" + maxQueueSizeInBits + ")");
    }

    @Override
    public OutputPort generate(NetworkDevice ownNetworkDevice, NetworkDevice towardsNetworkDevice, Link link) {
        return new BoundedPriorityOutputPort(ownNetworkDevice, towardsNetworkDevice, link, maxQueueSizeInBits);
    }

}
