package ch.ethz.systems.netbench.ext.flowlet;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.run.infrastructure.IntermediaryGenerator;

public class UniformFlowletIntermediaryGenerator extends IntermediaryGenerator {

    public UniformFlowletIntermediaryGenerator() {
        SimulationLogger.logInfo("Network device flowlet intermediary", "UNIFORM");
    }

    @Override
    public Intermediary generate(int networkDeviceIdentifier) {
        return new UniformFlowletIntermediary();
    }

}
