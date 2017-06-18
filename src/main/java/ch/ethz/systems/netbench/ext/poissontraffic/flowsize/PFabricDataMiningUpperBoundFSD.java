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
 * Expected flow size (upper bound):
 * 0.1*180+0.1*216+0.1*560+0.1*900+0.1*1100+0.1*1870+0.1*3160
   +0.1*10000+0.1*400000+0.05*3160000+0.03*100000000+0.02*1000000000
 * =
 * 23199798.6 bytes
 * At 10 Gbps would take 18.6ms
*/
public class PFabricDataMiningUpperBoundFSD extends FlowSizeDistribution {

    public PFabricDataMiningUpperBoundFSD() {
        super();
        SimulationLogger.logInfo("Flow planner flow size dist.", "pFabric data mining upper bound discrete");
    }

    @Override
    public long generateFlowSizeByte() {

        double outcome = independentRng.nextDouble();

        if (outcome >= 0.0 && outcome <= 0.1) {
            return 180;
        } else if (outcome >= 0.1 && outcome <= 0.2) {
            return 216;
        } else if (outcome >= 0.2 && outcome <= 0.3) {
            return 560;
        } else if (outcome >= 0.3 && outcome <= 0.4) {
            return 900;
        } else if (outcome >= 0.4 && outcome <= 0.5) {
            return 1100;
        } else if (outcome >= 0.5 && outcome <= 0.6) {
            return 1870;
        } else if (outcome >= 0.6 && outcome <= 0.7) {
            return 3160;
        } else if (outcome >= 0.7 && outcome <= 0.8) {
            return 10000;
        } else if (outcome >= 0.8 && outcome <= 0.9) {
            return 400000;
        } else if (outcome >= 0.9 && outcome <= 0.95) {
            return 3160000;
        } else if (outcome >= 0.95 && outcome <= 0.98) {
            return 100000000;
        } else { // outcome >= 0.98 && outcome <= 1.0
            return 1000000000;
        }

    }

}
