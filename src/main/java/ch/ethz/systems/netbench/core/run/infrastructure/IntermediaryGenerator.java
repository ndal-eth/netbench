package ch.ethz.systems.netbench.core.run.infrastructure;

import ch.ethz.systems.netbench.core.network.Intermediary;

public abstract class IntermediaryGenerator {
    public abstract Intermediary generate(int networkDeviceIdentifier);
}
