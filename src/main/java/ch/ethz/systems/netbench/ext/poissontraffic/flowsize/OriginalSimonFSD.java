package ch.ethz.systems.netbench.ext.poissontraffic.flowsize;

import ch.ethz.systems.netbench.core.log.SimulationLogger;

public class OriginalSimonFSD extends FlowSizeDistribution {

    public OriginalSimonFSD() {
        super();
        SimulationLogger.logInfo("Flow planner flow size dist.", "Original Simon");
    }

    @Override
    public long generateFlowSizeByte() {

        double outcome = independentRng.nextDouble();

        if (outcome >= 0.0 && outcome <= 0.2) {
            return 10000; // 10^4 bytes
        } else if (outcome >= 0.2 && outcome <= 0.6) {
            return 100000; // 10^5 bytes
        } else if (outcome >= 0.6 && outcome <= 0.73) {
            return 1000000; // 10^6 bytes
        } else if (outcome >= 0.73 && outcome <= 0.98) {
            return 10000000; // 10^7 bytes
        } else { // outcome >= 0.98 && outcome <= 1.0
            return 100000000; // 10^8 bytes
        }

    }

}
