package ch.ethz.systems.netbench.core.random;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RandomManager {

    private long universalSeed;
    private Set<String> issuedKeys;
    private Set<Long> issuedSeeds;

    /**
     * Constructor.
     *
     * @param universalSeed     Universal seed on which every random number generator is based off
     */
    public RandomManager(long universalSeed) {
        this.universalSeed = universalSeed;
        this.issuedKeys = new HashSet<>();
        this.issuedSeeds = new HashSet<>();
    }

    /**
     * Create a random number generator which as seed combines
     * the given string key with the universal seed passed in the
     * {@link #RandomManager(long) constructor}.
     *
     * The random number generator for a single key can only be requested once,
     * as else there are two identical random number generators (which is typically
     * very undesirable).
     *
     * @param key       String key
     *
     * @return Random number generator
     */
    public Random getRandom(String key) {

        // Keys are issued typically once
        if (issuedKeys.contains(key)) {
            throw new IllegalArgumentException("Unlikely wanted to ask for the same key twice in the same runtime.");
        }
        issuedKeys.add(key);

        // Generate issued seed
        long issuedSeed = 997 * universalSeed ^ 991 * key.hashCode();

        // Prevent that by chance it is a duplicate (safeguard)
        if (issuedSeeds.contains(issuedSeed)) {
            throw new IllegalArgumentException("Likely undesirable: duplicate seed created from two different keys.");
        }
        issuedSeeds.add(issuedSeed);

        // Create independent random number generator
        issuedSeeds.add(issuedSeed);
        return new Random(issuedSeed);

    }

}
