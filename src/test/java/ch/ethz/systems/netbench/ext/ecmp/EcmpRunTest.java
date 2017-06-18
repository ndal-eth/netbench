package ch.ethz.systems.netbench.ext.ecmp;

import ch.ethz.systems.netbench.core.run.MainFromProperties;
import ch.ethz.systems.netbench.testutility.TestLogReader;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class EcmpRunTest {

    @Test
    public void testEcmpN5Run() throws IOException {

        // Perform run (run folder: temp/test_ecmp_n5)
        String[] args = new String[]{"example/runs/test_ecmp_n5.properties"};
        MainFromProperties.main(args);

        // Fetch log mappings
        Map<Pair<Integer, Integer>, TestLogReader.PortUtilizationTuple> portQueueStateTupleMap = TestLogReader.getPortUtilizationMapping("temp/test_ecmp_n5");
        Map<Long, TestLogReader.FlowCompletionTuple> flowCompletionTupleMap = TestLogReader.getFlowCompletionMapping("temp/test_ecmp_n5");

        // Flow completion
        // They all share a single 10 Gbps link
        for (long i = 0; i < 10; i++) {
            TestLogReader.FlowCompletionTuple tuple = flowCompletionTupleMap.get(i);
            assertTrue((double) tuple.getSentBytes()*8 / (double) tuple.getDuration() <= 9.2);
            // TODO: More stringent requirements
        }

    }

    @Test
    public void testEcmpMultiN5Run() throws IOException {

        // Perform run (run folder: temp/test_ecmp_multi_n5)
        String[] args = new String[]{"example/runs/test_ecmp_multi_n5.properties"};
        MainFromProperties.main(args);

        // Fetch log mappings
        Map<Pair<Integer, Integer>, TestLogReader.PortUtilizationTuple> portQueueStateTupleMap = TestLogReader.getPortUtilizationMapping("temp/test_ecmp_multi_n5");
        Map<Long, TestLogReader.FlowCompletionTuple> flowCompletionTupleMap = TestLogReader.getFlowCompletionMapping("temp/test_ecmp_multi_n5");

        // Flow completion
        // There are three links
        for (long i = 0; i < flowCompletionTupleMap.size(); i++) {
            TestLogReader.FlowCompletionTuple tuple = flowCompletionTupleMap.get(i);
            assertTrue((double) tuple.getSentBytes()*8 / (double) tuple.getDuration() <= 9.2);
            // System.out.println(i + ": " + (double) tuple.getSentBytes()*8 / (double) tuple.getDuration());
        }

        // Utilization sum
        double utilSumToMid = 0.0;
        utilSumToMid += portQueueStateTupleMap.get(new ImmutablePair<>(0, 1)).getUtilizationNs();
        utilSumToMid += portQueueStateTupleMap.get(new ImmutablePair<>(0, 2)).getUtilizationNs();
        utilSumToMid += portQueueStateTupleMap.get(new ImmutablePair<>(0, 3)).getUtilizationNs();
        double utilSumMidToEnd = 0.0;
        utilSumMidToEnd += portQueueStateTupleMap.get(new ImmutablePair<>(1, 4)).getUtilizationNs();
        utilSumMidToEnd += portQueueStateTupleMap.get(new ImmutablePair<>(2, 4)).getUtilizationNs();
        utilSumMidToEnd += portQueueStateTupleMap.get(new ImmutablePair<>(3, 4)).getUtilizationNs();
        double utilSumEndToMid = 0.0;
        utilSumEndToMid += portQueueStateTupleMap.get(new ImmutablePair<>(4, 1)).getUtilizationNs();
        utilSumEndToMid += portQueueStateTupleMap.get(new ImmutablePair<>(4, 2)).getUtilizationNs();
        utilSumEndToMid += portQueueStateTupleMap.get(new ImmutablePair<>(4, 3)).getUtilizationNs();
        double utilSumMidToStart = 0.0;
        utilSumMidToStart += portQueueStateTupleMap.get(new ImmutablePair<>(1, 0)).getUtilizationNs();
        utilSumMidToStart += portQueueStateTupleMap.get(new ImmutablePair<>(2, 0)).getUtilizationNs();
        utilSumMidToStart += portQueueStateTupleMap.get(new ImmutablePair<>(3, 0)).getUtilizationNs();

        //
        // Sum of bytes per flow from flow origin:
        // 120 bytes SYN
        // 120 bytes ACK
        // 724 * 1500 bytes data packet (1000000 / 1380 = 724)
        // 880 + 120 bytes final data packet (1000000 % 1380 = 880)
        // ----------------------
        // 1087240 bytes = 8697920 bits
        //
        // There are 50 flows, so total amount of bits transferred:
        // 50 * 8697920 = 434896000
        //
        // This is done at a speed of 10 bit/ns (10 Gbps)
        //
        // So total time utilization: 434896000 / 10 = 43489600
        //
        assertEquals(43489600, utilSumToMid, 1e-6);
        assertEquals(utilSumToMid, utilSumMidToEnd, 1e-6);

        //
        // Sum of bytes per flow from flow endpoint:
        // 120 bytes ACK+SYN
        // 725 * 120 bytes ack packets
        // ----------------------
        // 87120 bytes = 696960 bits
        //
        // There are 50 flows, so total amount of bits transferred:
        // 50 * 696960 = 34848000
        //
        // This is done at a speed of 10 bit/ns (10 Gbps)
        //
        // So total time utilization: 34848000 / 10 = 3484800
        //
        assertEquals(3484800, utilSumEndToMid, 1e-6);
        assertEquals(utilSumEndToMid, utilSumMidToStart, 1e-6);

    }

}
