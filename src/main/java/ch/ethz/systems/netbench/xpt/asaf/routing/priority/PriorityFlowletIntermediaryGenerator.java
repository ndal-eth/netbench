package ch.ethz.systems.netbench.xpt.asaf.routing.priority;

import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.run.infrastructure.IntermediaryGenerator;
import ch.ethz.systems.netbench.ext.flowlet.FlowletIntermediary;

public class PriorityFlowletIntermediaryGenerator extends IntermediaryGenerator{

    public PriorityFlowletIntermediaryGenerator() {
        SimulationLogger.logInfo("Network device flowlet intermediary", "PRIORITY_UNIFORM_FLOWLET");
    }

    @Override
    public FlowletIntermediary generate(int networkDeviceIdentifier) {
        return new PriorityFlowletIntermediary();
    }

}
