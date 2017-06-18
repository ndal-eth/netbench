package ch.ethz.systems.netbench.ext.flowlet;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.run.infrastructure.IntermediaryGenerator;

public class IdentityFlowletIntermediaryGenerator extends IntermediaryGenerator {

    public IdentityFlowletIntermediaryGenerator() {
        SimulationLogger.logInfo("Network device flowlet intermediary", "IDENTITY");
    }

    @Override
    public Intermediary generate(int networkDeviceIdentifier) {
        return new IdentityFlowletIntermediary();
    }

}
