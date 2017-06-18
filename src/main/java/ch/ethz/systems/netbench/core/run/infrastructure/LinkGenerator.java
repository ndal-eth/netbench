package ch.ethz.systems.netbench.core.run.infrastructure;

import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Link;

public abstract class LinkGenerator {
    public abstract Link generate(NetworkDevice fromNetworkDevice, NetworkDevice toNetworkDevice);
}
