package ch.ethz.systems.netbench.ext.poissontraffic.flowsize;

import ch.ethz.systems.netbench.core.log.SimulationLogger;

/**
 * pFabric (Alizadeh, 2016) data mining flow size distribution.
 *
 * Size (byte)  Idx         Culc. prob.
 * 0            1           0
 * 180          2           0.1
 * 216          3           0.2
 * 560          4           0.3
 * 900          5           0.4
 * 1100         6           0.5
 * 1870         7           0.6
 * 3160         8           0.7
 * 10000        9           0.8
 * 400000       10          0.9
 * 3.16e+06     11          0.95
 * 1e+08        12          0.98
 * 1e+09        13          1
 *
 * Expected flow size (lower bound):
 * 0.1*1+0.1*180+0.1*216+0.1*560+0.1*900+0.1*1100+0.1*1870
   +0.1*3160+0.1*10000+0.05*400000+0.03*3160000+0.02*100000000
 * =
 * 2116598.7 bytes
 * At 10 Gbps would take 1.69ms
 *
 * NOTE: the 1 is because non-empty flows don't make any sense
 */
public class PFabricDataMiningLowerBoundFSD extends FlowSizeDistribution {

    public PFabricDataMiningLowerBoundFSD() {
        super();
        SimulationLogger.logInfo("Flow planner flow size dist.", "pFabric data mining lower bound discrete");
    }

    @Override
    public long generateFlowSizeByte() {

        double outcome = independentRng.nextDouble();

        if (outcome >= 0.0 && outcome <= 0.1) {
            return 1;
        } else if (outcome >= 0.1 && outcome <= 0.2) {
            return 180;
        } else if (outcome >= 0.2 && outcome <= 0.3) {
            return 216;
        } else if (outcome >= 0.3 && outcome <= 0.4) {
            return 560;
        } else if (outcome >= 0.4 && outcome <= 0.5) {
            return 900;
        } else if (outcome >= 0.5 && outcome <= 0.6) {
            return 1100;
        } else if (outcome >= 0.6 && outcome <= 0.7) {
            return 1870;
        } else if (outcome >= 0.7 && outcome <= 0.8) {
            return 3160;
        } else if (outcome >= 0.8 && outcome <= 0.9) {
            return 10000;
        } else if (outcome >= 0.9 && outcome <= 0.95) {
            return 400000;
        } else if (outcome >= 0.95 && outcome <= 0.98) {
            return 3160000;
        } else { // outcome >= 0.98 && outcome <= 1.0
            return 100000000;
        }

    }

}
