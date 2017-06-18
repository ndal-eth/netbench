package ch.ethz.systems.netbench.core.utility;

public class UnitConverter {

    private UnitConverter() {
        // Cannot be instantiated
    }

    public static long convertSecondsToNanoseconds(double seconds) {
        return (long) (seconds * 1000000000L);
    }

    public static long convertSecondsToNanoseconds(long seconds) {
        return seconds * 1000000000L;
    }

}
