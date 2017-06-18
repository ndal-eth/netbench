package ch.ethz.systems.netbench.core.network;

import ch.ethz.systems.netbench.core.Simulator;

public abstract class Event implements Comparable<Event> {

    // Added for absolute determinism in the event priority queue
    private static long c = 0;
    private final long eid;

    // Time to trigger
    private final long time;

    /**
     * Create event which will happen the given amount of nanoseconds later.
     *
     * @param timeFromNowNs     Time it will take before happening from now in nanoseconds
     */
    public Event(long timeFromNowNs) {
        this.time = Simulator.getTimeFromNow(timeFromNowNs);
        this.eid = c;
        c++;
    }

    /**
     * Trigger whatever has to happen with the event
     * and the data it contains.
     */
    public abstract void trigger();

    /**
     * Retrieve absolute simulation time at which the
     * event must occur.
     *
     * @return  Absolute simulation event time
     */
    public long getTime() {
        return time;
    }

    @Override
    public int compareTo(Event o) {
        return (this.time < o.time ? -1 : (this.time == o.time ? (this.eid < o.eid ? -1 : (this.eid == o.eid ? 0 : 1)) : 1));
    }

}