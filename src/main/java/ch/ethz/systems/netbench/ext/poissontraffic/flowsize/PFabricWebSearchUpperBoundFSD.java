package ch.ethz.systems.netbench.ext.poissontraffic.flowsize;

import ch.ethz.systems.netbench.core.log.SimulationLogger;

/**
 * pFabric (Alizadeh, 2016) web search flow distribution.
 *
 * Size (byte)  Idx     Culc. prob.
 * 0            1       0
 * 10000        2       0.15
 * 20000        3       0.2
 * 30000        4       0.3
 * 50000        5       0.4
 * 80000        6       0.53
 * 200000       7       0.6
 * 1e+06        8       0.7
 * 2e+06        9       0.8
 * 5e+06        10      0.9
 * 1e+07        11      0.97
 * 3e+07        12      1
 *
 * Expected flow size (upper bound):
 * 0.15*10000+0.05*20000+0.1*30000+0.1*50000+0.13*80000+0.07*200000
   +0.1*1000000+0.1*2000000+0.1*5000000+0.07*10000000+0.03*30000000
 * =
 * 2434900 bytes
 * At 10 Gbps would take 1.95ms
 *
 */
public class PFabricWebSearchUpperBoundFSD extends FlowSizeDistribution {

    public PFabricWebSearchUpperBoundFSD() {
        super();
        SimulationLogger.logInfo("Flow planner flow size dist.", "pFabric web search upper bound discrete");
    }

    @Override
    public long generateFlowSizeByte() {

        double outcome = independentRng.nextDouble();

        if (outcome >= 0.0 && outcome <= 0.15) {
            return 10000;
        } else if (outcome >= 0.15 && outcome <= 0.2) {
            return 20000;
        } else if (outcome >= 0.2 && outcome <= 0.3) {
            return 30000;
        } else if (outcome >= 0.3 && outcome <= 0.4) {
            return 50000;
        } else if (outcome >= 0.4 && outcome <= 0.53) {
            return 80000;
        } else if (outcome >= 0.53 && outcome <= 0.6) {
            return 200000;
        } else if (outcome >= 0.6 && outcome <= 0.7) {
            return 1000000;
        } else if (outcome >= 0.7 && outcome <= 0.8) {
            return 2000000;
        } else if (outcome >= 0.8 && outcome <= 0.9) {
            return 5000000;
        } else if (outcome >= 0.9 && outcome <= 0.97) {
            return 10000000;
        } else { // outcome >= 0.97 && outcome <= 1.0
            return 30000000;
        }

    }

}
