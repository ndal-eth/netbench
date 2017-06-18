package ch.ethz.systems.netbench.core.log;

public interface LoggerCallback {

    /**
     * Called by the {@link SimulationLogger} just before
     * all streams are closed. Can be used e.g. to write away overall statistics.
     */
    void callBeforeClose();

}
