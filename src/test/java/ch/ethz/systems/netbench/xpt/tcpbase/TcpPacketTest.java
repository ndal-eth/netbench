package ch.ethz.systems.netbench.xpt.tcpbase;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TcpPacketTest {

    @Before
    public void setup() {
        Simulator.setup(0);
    }

    @After
    public void cleanup() {
        Simulator.reset();
    }

    @Test
    public void simpleInst() {

        // Run for some time to have an interesting departure time set
        Simulator.runNs(37777);

        // Create packet
        FullExtTcpPacket packet = new FullExtTcpPacket(
                46622, 3535, 67, 89,
                100, 80, 81, // TTL, source port, destination port
                473737, 373, // Seq number, Ack number
                false, false, false, false, true, false, false, false, false,
                // NS  CWR    ECE    URG    ACK   PSH    RST    SYN    FIN
                4262, 892 // Window size
        );

        // Default getters
        assertEquals(packet.getFlowId(), 46622);
        assertEquals(packet.getDataSizeByte(), 3535);
        assertEquals(packet.getSizeBit(), 3535 * 8 + 60 * 8 + 60 * 8);
        assertEquals(packet.getSourceId(), 67);
        assertEquals(packet.getDestinationId(), 89);
        assertEquals(packet.getTTL(), 100);
        assertEquals(packet.getSourcePort(), 80);
        assertEquals(packet.getDestinationPort(), 81);
        assertEquals(packet.getSequenceNumber(), 473737);
        assertEquals(packet.getAcknowledgementNumber(), 373);
        assertFalse(packet.isNS());
        assertFalse(packet.isCWR());
        assertFalse(packet.isECE());
        assertFalse(packet.isURG());
        assertTrue(packet.isACK());
        assertFalse(packet.isPSH());
        assertFalse(packet.isRST());
        assertFalse(packet.isSYN());
        assertFalse(packet.isFIN());
        assertEquals(packet.getWindowSize(), 4262.0, 0.0);
        assertEquals(packet.getDepartureTime(), 37777);
        assertEquals(0, packet.getFlowletId());
        packet.setFlowletId(352555);
        assertEquals(352555, packet.getFlowletId());
        assertEquals(892, packet.getPriority());

        // Congestion encountering
        assertFalse(packet.getECN());
        packet.markCongestionEncountered();
        assertTrue(packet.getECN());
        packet.markCongestionEncountered();
        assertTrue(packet.getECN());

        // Time-to-live (TTL) check
        for (int i = 0; i < 100; i++) {
            assertEquals(100 - i, packet.getTTL());
            boolean isDead = packet.decrementTtlAndIsDead();
            assertEquals(100 - i - 1, packet.getTTL());
            if (i == 99) {
                assertTrue(isDead);
            } else {
                assertFalse(isDead);
            }
        }

        // Print string
        System.out.println(packet.toString());

        // Set flowlet identifier
        packet.setEchoFlowletId(24252);
        assertEquals(24252, packet.getEchoFlowletId());

    }

    @Test
    public void testHashingQualityRandom() {

        Random r = new Random(3535);

        ArrayList<Integer> counters = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            counters.add(0);
        }

        for (int i = 0; i < 100000; i++) {

            // Create packet
            TcpPacket packet1 = new FullExtTcpPacket(
                    r.nextLong(), 3535, r.nextInt(), r.nextInt(),
                    100, 80, 81, // TTL, source port, destination port
                    473737, 373, // Seq number, Ack number
                    false, false, false, false, true, false, false, false, false,
                    // NS  CWR    ECE    URG    ACK   PSH    RST    SYN    FIN
                    4262, 0 // Window size
            );
            packet1.setFlowletId(r.nextInt());
            packet1.setHashSrcDstFlowletDependent();
            int h = packet1.getHash(0) % 100;
            counters.set(h, counters.get(h) + 1);

        }

        for (int i = 0; i < 100; i++) {
            if (counters.get(i) < 500) {
                throw new RuntimeException("Extremely poor hashing quality; one of the possibilities has less than half of its expectation.");
            }
            // System.out.println(i + ": " + counters.get(i));
        }

    }

    @Test
    public void testHashingQualitySequential() {

        ArrayList<Integer> counters = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            counters.add(0);
        }

        for (int i = 0; i < 100000; i++) {

            // Create packet
            TcpPacket packet1 = new FullExtTcpPacket(
                    3774, 3535, 43, 55,
                    100, 80, 81, // TTL, source port, destination port
                    473737, 373, // Seq number, Ack number
                    false, false, false, false, true, false, false, false, false,
                    // NS  CWR    ECE    URG    ACK   PSH    RST    SYN    FIN
                    4262, 0 // Window size
            );
            packet1.setFlowletId(i);
            packet1.setHashSrcDstFlowFlowletDependent();
            int h = packet1.getHash(0) % 100;
            counters.set(h, counters.get(h) + 1);

        }

        for (int i = 0; i < 100; i++) {
            if (counters.get(i) < 500) {
                throw new RuntimeException("Extremely poor hashing quality; one of the possibilities has less than half of its expectation.");
            }
            // System.out.println(i + ": " + counters.get(i));
        }

    }

    @Test
    public void testHashingQualityRandomDoubleSequence() {

        // Generate a couple of flow identifiers for the two flows
        Random r = new Random(3535);
        List<Integer> listA = new ArrayList<>();
        List<Integer> listB = new ArrayList<>();
        listA.add(0);
        listB.add(1);
        listA.add(5);
        listB.add(6);
        for (int i = 0; i < 10; i++) {
            listA.add(Math.abs(r.nextInt()) % 300);
            int other = Math.abs(r.nextInt()) % 300;
            while (other == listA.get(i)) {
                other = Math.abs(r.nextInt()) % 300;
            }
            listB.add(other);
        }

        // Go over all chosen pairs of flow identifiers
        for (int z = 0; z < listA.size(); z++) {

            // Basically some default state information
            int[] baseFlowIds = new int[]{listA.get(z), listB.get(z)};
            int srcId = 35;
            int dstId = 98;
            int extraFactor = 32;

            // Statistical power expectations
            int outcomes = 7;
            int sequenceLength = 1000;

            // Initialize counters
            List<Integer> counters = new ArrayList<>();
            List<List<Integer>> sequences = new ArrayList<>();
            for (int i = 0; i < outcomes; i++) {
                counters.add(0);
                sequences.add(new ArrayList<Integer>());
            }

            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < sequenceLength; j++) {

                    // Create packet
                    TcpPacket packet1 = new FullExtTcpPacket(
                            baseFlowIds[i], 3535, srcId, dstId,
                            100, 80, 81, // TTL, source port, destination port
                            473737, 373, // Seq number, Ack number
                            false, false, false, false, true, false, false, false, false,
                            // NS  CWR    ECE    URG    ACK   PSH    RST    SYN    FIN
                            4262, 0 // Window size
                    );
                    packet1.setFlowletId(j);
                    packet1.setHashSrcDstFlowFlowletDependent();
                    int h = packet1.getHash(extraFactor) % outcomes;
                    counters.set(h, counters.get(h) + 1);
                    sequences.get(i).add(h);
                    // To print sequences: System.out.print(h + "-");
                }
                // To print sequences: System.out.println();
            }

            // Calculate overlap
            int same = 0;
            for (int i = 0; i < sequenceLength; i++) {
                if (sequences.get(0).get(i).equals(sequences.get(1).get(i))) {
                    same++;
                }
            }
            //System.out.println("Sequences overlap for " + (((double) same) / sequenceLength * 100) + "% which should be close to the expected " + (1.0 / outcomes * 100) + "%");
            double deltaPercentage = Math.abs(((((double) same) / sequenceLength * 100) - (1.0 / outcomes * 100)));
            if (deltaPercentage > 5) {
                throw new RuntimeException(
                        "Sequence overlap (which was " + (((double) same) / sequenceLength * 100) +
                         "%) was more than 3% different from the expectation (which is " + (1.0 / outcomes * 100) +
                         "%), which is not allowed."
                );
            }

            // Outcome sum of sequences
            // System.out.println("Sum of outcomes for both sequences: ");
            for (int c = 0; c < outcomes; c++) {
                // System.out.println(c + ": " + counters.get(c));
                double delta = Math.abs(1 - counters.get(c) / ((double) 2 * sequenceLength / (double) outcomes)) * 100;
                if (delta > 15) {
                    System.out.println("Expectation count: " + (((double) 2 * sequenceLength / (double) outcomes)));
                    System.out.println("Actual count: " + counters.get(c));
                    throw new RuntimeException("Outcomes deviate (" + delta + "%) more than 15% from expectation, not allowed.");
                }
            }

        }

    }

}
