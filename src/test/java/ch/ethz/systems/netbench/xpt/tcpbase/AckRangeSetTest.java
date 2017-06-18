package ch.ethz.systems.netbench.xpt.tcpbase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class AckRangeSetTest {

    @Test
    public void testTcpScenario() {

        AckRangeSet set = new AckRangeSet();

        // SYN bit from initial SYN received
        assertEquals(1, set.determineReceiveNextNumber(1));

        // ACK has been received
        assertEquals(1, set.determineReceiveNextNumber(1));

        // Two packets are received correctly
        assertEquals(101, set.determineReceiveNextNumber(101));
        assertEquals(201, set.determineReceiveNextNumber(201));

        // 201-301 packet is missing, but 301-401 arrived
        set.add(301, 401);

        // Now 201-301 has arrived
        assertEquals(401, set.determineReceiveNextNumber(301));

        // No ranges should be left
        List<AckRange> rangesList = convertToOrderedList(set.getRanges());
        assertEquals(0, rangesList.size());

    }

    @Test
    public void testRangeIsWithin() {

        AckRangeSet set = new AckRangeSet();
        set.add(101, 301);
        ArrayList<AckRange> arr = (ArrayList<AckRange>) set.createSelectiveAckData();

        assertFalse(arr.get(0).isWithin(0, 1));
        assertFalse(arr.get(0).isWithin(1, 101));
        assertTrue(arr.get(0).isWithin(101, 201));
        assertTrue(arr.get(0).isWithin(201, 301));
        assertTrue(arr.get(0).isWithin(101, 301));
        assertFalse(arr.get(0).isWithin(301, 401));
        assertFalse(arr.get(0).isWithin(401, 501));

    }

    @Test
    public void testToString() {
        AckRangeSet set = new AckRangeSet();
        set.add(101, 301);
        ArrayList<AckRange> arr = (ArrayList<AckRange>) set.createSelectiveAckData();
        arr.get(0).toString();
        set.toString();
    }

    @Test
    public void testRangeMergeOnly() {

        AckRangeSet set = new AckRangeSet();
        set.add(0, 1);
        set.add(1, 101);
        set.add(101, 201);
        set.add(201, 301);
        set.add(301, 401);
        set.add(201, 301);
        set.add(501, 601);

        List<AckRange> rangesList = convertToOrderedList(set.getRanges());
        assertEquals(2, rangesList.size());
        assertRangeEquals(0, 401, rangesList.get(0));
        assertRangeEquals(501, 601, rangesList.get(1));

    }

    @Test
    public void testRangeMergeAbove() {

        AckRangeSet set = new AckRangeSet();
        set.add(0, 1);
        set.add(1, 101);
        set.add(201, 301);
        set.add(101, 201);
        set.add(401, 501);

        List<AckRange> rangesList = convertToOrderedList(set.getRanges());
        assertEquals(2, rangesList.size());
        assertRangeEquals(0, 301, rangesList.get(0));
        assertRangeEquals(401, 501, rangesList.get(1));

    }

    private List<AckRange> convertToOrderedList(Collection<AckRange> ranges) {
        List<AckRange> rangesList = new ArrayList<>();
        rangesList.addAll(ranges);
        Collections.sort(rangesList, new Comparator<AckRange>() {
            @Override
            public int compare(AckRange o1, AckRange o2) {
                return ((Long) o1.getLowBound()).compareTo(o2.getLowBound());
            }
        });
        return rangesList;
    }

    private void assertRangeEquals(long lowBound, long highBound, AckRange range) {
        assertEquals(lowBound, range.getLowBound());
        assertEquals(highBound, range.getHighBound());
    }

    @Test
    public void testImmutability() {

        // 0-301, 401-501
        AckRangeSet set = new AckRangeSet();
        set.add(0, 1);
        set.add(1, 101);
        set.add(201, 301);
        set.add(101, 201);
        set.add(401, 501);

        // Create ack data
        List<AckRange> l = (List<AckRange>) set.createSelectiveAckData();

        // Modify ack range set
        set.add(501, 601);

        List<AckRange> l2 = (List<AckRange>) set.createSelectiveAckData();

        assertEquals(2, l.size());
        assertRangeEquals(0, 301, l.get(0));
        assertRangeEquals(401, 501, l.get(1));

        assertEquals(2, l2.size());
        assertRangeEquals(0, 301, l2.get(0));
        assertRangeEquals(401, 601, l2.get(1));

        // Modify ack range set
        set.add(801, 901);

        List<AckRange> l3 = (List<AckRange>) set.createSelectiveAckData();

        assertEquals(2, l.size());
        assertRangeEquals(0, 301, l.get(0));
        assertRangeEquals(401, 501, l.get(1));

        assertEquals(2, l2.size());
        assertRangeEquals(0, 301, l2.get(0));
        assertRangeEquals(401, 601, l2.get(1));

        assertEquals(3, l3.size());
        assertRangeEquals(0, 301, l3.get(0));
        assertRangeEquals(401, 601, l3.get(1));
        assertRangeEquals(801, 901, l3.get(2));

        l3.add(new AckRange(1001, 1101));

        List<AckRange> l4 = (List<AckRange>) set.createSelectiveAckData();

        assertEquals(3, l4.size());
        assertRangeEquals(0, 301, l4.get(0));
        assertRangeEquals(401, 601, l4.get(1));
        assertRangeEquals(801, 901, l4.get(2));

    }

}
