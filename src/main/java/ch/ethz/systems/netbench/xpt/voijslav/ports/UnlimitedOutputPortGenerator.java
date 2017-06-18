package ch.ethz.systems.netbench.xpt.voijslav.ports;


import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.run.infrastructure.OutputPortGenerator;

public class UnlimitedOutputPortGenerator  extends OutputPortGenerator {

    public UnlimitedOutputPortGenerator() {
        SimulationLogger.logInfo("Port", "UNLIMITED_PORT");
    }

    @Override
    public OutputPort generate(NetworkDevice ownNetworkDevice, NetworkDevice towardsNetworkDevice, Link link) {
        return new UnlimitedOutputPort(ownNetworkDevice, towardsNetworkDevice, link);
    }

}