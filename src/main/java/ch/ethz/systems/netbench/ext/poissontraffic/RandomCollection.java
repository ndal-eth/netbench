package ch.ethz.systems.netbench.ext.poissontraffic;


import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class RandomCollection<E> {

    private final NavigableMap<Double, E> map;
    private final Random random;
    private double total;

    /**
     * Instantiate random collection.
     *
     * Guarantees same outcome with the given random number generator when
     * adding and drawing elements in exactly the same sequence.
     *
     * @param random    Random number generator instance
     */
    public RandomCollection(Random random) {
        this.map = new TreeMap<>();
        this.random = random;
        this.total = 0;
    }

    /**
     * Add a new element with the given weight.
     *
     * @param weight    Probability weight of the element
     * @param result    Element instance of what draw should result in
     */
    public void add(double weight, E result) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Cannot add negative weight (" + weight + ") to random collection.");
        }
        total += weight;
        map.put(total, result);
    }

    /**
     * Independently draw, respecting the weights, a next element out of the collection.
     * The collection must have a total weight extremely close to 1.
     *
     * @return  Independently drawn element out of the collection
     */
    public E next() {
        if (Math.abs(1.0 - total) > 1e-6) {
            throw new IllegalArgumentException("Total weight (" + total + ") in random collection differs too much (> 1e-6) from 1.");
        }
        double value = random.nextDouble();
        return map.ceilingEntry(value).getValue();
    }

}
