package ch.ethz.systems.netbench.ext.poissontraffic.flowsize;

import ch.ethz.systems.netbench.ext.poissontraffic.ParetoDistribution;

/**
 * Pareto flow size distribution.
 *
 * @author Asaf Valdarsky
 * @author Simon Kassing
 */
public class ParetoFSD extends FlowSizeDistribution {

    private ParetoDistribution distribution;

    /**
     * Pareto Flow Size Distribution (FSD).
     *
     * @param shape                     Shape parameter (the higher the more skewed the distribution becomes)
     * @param meanFlowSizeKiloBytes     Mean flow size in kilo bytes (KB)
     */
    public ParetoFSD(double shape, long meanFlowSizeKiloBytes) {

        // Illegal shape parameter
        if (shape <= 0.0) {
            throw new IllegalArgumentException("Pareto distribution's shape parameter must be in (0, inf).");
        }

        // Illegal mean flow size (KB)
        if (meanFlowSizeKiloBytes < 1) {
            throw new IllegalArgumentException("Pareto distribution's mean flow size (KB) parameter must be in [1, inf).");
        }

        // Convert to proper Pareto distribution parameters
        long meanFlowSizeBytes = meanFlowSizeKiloBytes * 1000;
        double scale = (meanFlowSizeBytes * (shape - 1)) / shape;

        // Create distribution
        this.distribution = new ParetoDistribution(shape, scale, this.independentRng);

    }
    
    /**
     * Independently generate a flow size drawn from the Pareto distribution.
     *
     * @return  Flow size drawn from parametrized Pareto distribution
     */
    @Override
    public long generateFlowSizeByte() {
        long oneGigabyte = 1024*1024*1024;
        return Math.min((long) this.distribution.draw(), oneGigabyte);
    }

}
