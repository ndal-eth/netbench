package ch.ethz.systems.netbench.ext.poissontraffic;

import java.util.Random;

/**
 * Pareto distribution.
 *
 * @author Asaf Valdarsky
 * @author Simon Kassing
 */
public class ParetoDistribution {

    private double shape;
    private double scale;
    private Random random;

    public ParetoDistribution(double shape, double scale, Random random) {

        // Illegal shape parameter
        if (shape <= 0.0) {
            throw new IllegalArgumentException("Pareto distribution's shape parameter must be in (0, inf).");
        }

        // Illegal scale parameter
        if (scale <= 0.0) {
            throw new IllegalArgumentException("Pareto distribution's scale parameter must be in (0, inf).");
        }

        // Set parameters
        this.shape = shape;
        this.scale = scale;
        this.random = random;

    }

    /**
     * Inverse of the CDF of a Pareto distribution:
     * CDF = 1 - (scale / x)^shape
     * 1 - CDF = (scale / x)^shape
     * (1 - CDF)^(1/shape) = scale / x
     * x = scale / ((1 - CDF)^(1/shape))
     *
     * Because outcome is drawn uniformly from [0, 1.0),
     * (1 - CDF) is replaced with the outcome.
     *
     * @return  Value drawn from parametrized Pareto distribution
     */
    public double draw() {
        double outcome = random.nextDouble();
        return (scale / Math.pow(outcome, 1.0 / shape));
    }

}
