package ch.ethz.systems.netbench.ext.poissontraffic.flowsize;

/**
 * Uniform flow size distribution.
 *
 * @author Simon Kassing
 */
public class UniformFSD extends FlowSizeDistribution {

    private long uniformFlowSizeBytes;

    /**
     * Uniform flow size distribution.
     *
     * @param uniformFlowSizeBytes      Shape parameter (the higher the more skewed the distribution becomes)
     */
    public UniformFSD(long uniformFlowSizeBytes) {

        // Illegal mean flow size (KB)
        if (uniformFlowSizeBytes < 1) {
            throw new IllegalArgumentException("Uniform flow size (KB) parameter must be in [1, inf).");
        }

        // Create distribution
        this.uniformFlowSizeBytes = uniformFlowSizeBytes;

    }

    /**
     * Independently generate a flow size drawn from the uniform distribution.
     *
     * @return  Uniform flow size
     */
    @Override
    public long generateFlowSizeByte() {
        return uniformFlowSizeBytes;
    }

}
