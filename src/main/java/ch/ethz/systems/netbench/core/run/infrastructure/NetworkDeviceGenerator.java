package ch.ethz.systems.netbench.core.run.infrastructure;

import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.TransportLayer;

public abstract class NetworkDeviceGenerator {
    public abstract NetworkDevice generate(int identifier);
    public abstract NetworkDevice generate(int identifier, TransportLayer server);
}
