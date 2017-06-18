package ch.ethz.systems.netbench.xpt.utility;

public class TwoExclusionRange {

    private final int range;
    private final int lowerBoundIncl;
    private final int upperBoundIncl;
    private final int exclusions;
    private final Integer exclusionA;

    /**
     * Create a range that can possibly exclude two of its candidates that are in the range.
     *
     * @param lowerBoundIncl        Lower bound of the range (inclusive)
     * @param upperBoundIncl        Higher bound of the range (inclusive)
     * @param exclusionCandidate    Candidate which will be excluded if it exists in the range
     */
    public TwoExclusionRange(int lowerBoundIncl, int upperBoundIncl, int exclusionCandidate) {
        assert(upperBoundIncl >= lowerBoundIncl);

        // General range data
        this.range = upperBoundIncl - lowerBoundIncl + 1;
        this.lowerBoundIncl = lowerBoundIncl;
        this.upperBoundIncl = upperBoundIncl;

        // Add first possible exclusion in the range
        if (exclusionCandidate >= lowerBoundIncl && exclusionCandidate <= upperBoundIncl) {
            this.exclusions = 1;
            this.exclusionA = exclusionCandidate;
        } else {
            this.exclusions = 0;
            this.exclusionA = null;
        }

    }

    /**
     * Draw the correct candidate out of the range given the hash.
     *
     * @param hash                  Hash
     * @param exclusionCandidate    Exclusion candidate
     *
     * @return  Candidate matching the hash
     */
    public int draw(int hash, int exclusionCandidate) {

        // Create local copy
        Integer tempA = exclusionA;
        Integer tempB = null;
        int tempExcl = exclusions;

        // Add the exclusion candidate if it exists in the bounds
        if (exclusionCandidate >= lowerBoundIncl && exclusionCandidate <= upperBoundIncl) {
            if (tempExcl == 0) {
                tempExcl++;
                tempA = exclusionCandidate;
            } else {
                tempExcl++;
                tempB = Math.max(tempA, exclusionCandidate);
                tempA = Math.min(tempA, exclusionCandidate);
            }
        }

        // Draw a result in the bounds
        int result = this.lowerBoundIncl + hash % (this.range - tempExcl);

        // Skip the excluded candidates
        if (tempExcl == 1) {
            if (result >= tempA) {
                result = result + 1;
            }
        } else if (tempExcl == 2) {
            if (result >= tempA) {
                result++;
                if (result >= tempB) {
                    result++;
                }
            }
        }

        // Return adapted result
        return result;

    }

}
