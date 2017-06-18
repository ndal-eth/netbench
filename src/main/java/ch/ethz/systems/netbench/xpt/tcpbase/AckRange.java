package ch.ethz.systems.netbench.xpt.tcpbase;

/**
 * Acknowledgment range (immutable object).
 */
public class AckRange {

    final long lowBound;
    final long highBound;

    AckRange(long lowBound, long highBound) {
        this.lowBound = lowBound;
        this.highBound = highBound;
    }

    long getLowBound() {
        return lowBound;
    }

    long getHighBound() {
        return highBound;
    }

    public String toString() {
        return "[" + lowBound + ", " + highBound + ")";
    }

    public boolean isWithin(long intLowBound, long intHighBound) {
        return intLowBound >= this.lowBound  && intHighBound <= this.highBound;
    }

}