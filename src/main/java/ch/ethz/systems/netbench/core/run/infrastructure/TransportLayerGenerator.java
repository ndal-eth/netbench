package ch.ethz.systems.netbench.core.run.infrastructure;


import ch.ethz.systems.netbench.core.network.TransportLayer;

public abstract class TransportLayerGenerator {
    public abstract TransportLayer generate(int identifier);
}
