package ch.ethz.systems.netbench.xpt.tcpbase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AckRangeSet {

    // Mappings to ranges
    private Map<Long, AckRange> lowBoundToRange;
    private Map<Long, AckRange> highBoundToRange;
    private Collection<AckRange> ranges;

    // Caching of the range set which is passed along to
    // the packets with SACK Options
    private boolean cacheIsInvalid;
    private Collection<AckRange> cacheRanges;

    /**
     * Construct empty acknowledgment range set.
     */
    public AckRangeSet() {
        lowBoundToRange = new HashMap<>();
        highBoundToRange = new HashMap<>();
        ranges = lowBoundToRange.values();
        this.cacheIsInvalid = true;
        this.cacheRanges = null;
    }

    /**
     * Determine the receive next number based on what it is currently.
     * Basically it checks if there is not a range which has now become
     * obsolete as the next packet has come in.
     *
     * @param receiveNextNumber     Receive next number after a left-most packet has been received
     *
     * @return Correct next number desired to receive, potentially advanced by a range
     */
    public long determineReceiveNextNumber(long receiveNextNumber) {
        AckRange rangeFound = lowBoundToRange.get(receiveNextNumber);
        if (rangeFound != null) {
            lowBoundToRange.remove(receiveNextNumber);
            receiveNextNumber = rangeFound.highBound;
            highBoundToRange.remove(rangeFound.highBound);
            cacheIsInvalid = true;
        }
        return receiveNextNumber;
    }

    /**
     * Adds a confirmed packet sequence number and acknowledgment number.
     *
     * @param seqNumber     Sequence number
     * @param ackNumber     Acknowledgment number
    */
    public void add(long seqNumber, long ackNumber) {
        assert(seqNumber >= 0L && ackNumber >= 0L);

        // If it is already acknowledged, ignore it (could be improved to an interval tree)
        for (AckRange range : ranges) {
            if (seqNumber >= range.lowBound && ackNumber <= range.highBound) {
                return;
            }
        }

        // Create range
        AckRange range = new AckRange(seqNumber, ackNumber);

        // Merge with a range above it if they touch
        AckRange aboveRange = lowBoundToRange.get(ackNumber);
        if (aboveRange != null) {
            range = merge(range, aboveRange);
        }

        // Merge with a range below it if they touch
        AckRange belowRange = highBoundToRange.get(seqNumber);
        if (belowRange != null) {
            range = merge(belowRange, range);
        }

        // Register if it did not find in either range
        if (aboveRange == null && belowRange == null) {
            lowBoundToRange.put(seqNumber, range);
            highBoundToRange.put(ackNumber, range);
        }

        // Either a range is added or merged, so it is now invalid
        cacheIsInvalid = true;

    }

    /**
     * Merge two ranges into each other in which range A is
     * below range B and touches it. E.g.:
     *
     * A: [400, 500]
     * B: [500, 700]
     *
     * @param rangeA    Below range
     * @param rangeB    Above range
     */
    private AckRange merge(AckRange rangeA, AckRange rangeB) {

        // Remove ranges
        lowBoundToRange.remove(rangeA.lowBound);
        highBoundToRange.remove(rangeA.highBound);
        lowBoundToRange.remove(rangeB.lowBound);
        highBoundToRange.remove(rangeB.highBound);

        // Instantiate new range
        AckRange range = new AckRange(rangeA.lowBound, rangeB.highBound);
        lowBoundToRange.put(range.lowBound, range);
        highBoundToRange.put(range.highBound, range);
        return range;

    }

    /**
     * Create a minimal copy of the range set to sent along with packets.
     *
     * @return  Copy of the acknowledgment range set
     */
    public Collection<AckRange> createSelectiveAckData() {
        if (cacheIsInvalid) {
            cacheRanges = new ArrayList<>(ranges);
        }
        return cacheRanges;
    }

    /**
     * Retrieve ranges (only for testing).
     *
     * @return  Current ranges
     */
    Collection<AckRange> getRanges() {
        return ranges;
    }

    @Override
    public String toString() {
        return ranges.toString();
    }

}
