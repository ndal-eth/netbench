package ch.ethz.systems.netbench.ext.demo;

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class DemoRunTest {

    @Test
    public void testDemoRun() throws IOException {

        // Perform run (run folder: temp/test_n2)
        String[] args = new String[]{"example/runs/test_n2.properties"};
        MainFromProperties.main(args);

        // Fetch log mappings
        Map<Pair<Integer, Integer>, TestLogReader.PortUtilizationTuple> portQueueStateTupleMap = TestLogReader.getPortUtilizationMapping("temp/test_n2");
        Map<Long, TestLogReader.FlowCompletionTuple> flowCompletionTupleMap = TestLogReader.getFlowCompletionMapping("temp/test_n2");

        // Flow completion
        TestLogReader.FlowCompletionTuple tuple = flowCompletionTupleMap.get(0L);
        assertEquals(9479, tuple.getEndTime());
        assertFalse(tuple.isCompleted());
        assertEquals(9479, tuple.getDuration());
        assertEquals(4000, tuple.getSentBytes());
        assertEquals(1000000000L, tuple.getTotalSizeBytes());

        // Port utilization

        // 0 -> 1
        assertEquals((5*848) / 9479.0 * 100.0, portQueueStateTupleMap.get(new ImmutablePair<>(0, 1)).getUtilizationPercentage(), 1e-6);
        assertEquals((5*848), portQueueStateTupleMap.get(new ImmutablePair<>(0, 1)).getUtilizationNs());

        // 1 -> 0
        assertEquals((5*48) / 9479.0 * 100.0, portQueueStateTupleMap.get(new ImmutablePair<>(1, 0)).getUtilizationPercentage(), 1e-6);
        assertEquals((5*48), portQueueStateTupleMap.get(new ImmutablePair<>(1, 0)).getUtilizationNs());

    }

    @Test
    public void testDemoRunWithFlowThreshold() throws IOException {

        // Perform run (run folder: temp/test_n2_flow_threshold)
        String[] args = new String[]{"example/runs/test_n2_flow_threshold.properties"};
        MainFromProperties.main(args);

        // Fetch log mappings
        Map<Pair<Integer, Integer>, TestLogReader.PortUtilizationTuple> portQueueStateTupleMap = TestLogReader.getPortUtilizationMapping("temp/test_n2_flow_threshold");
        Map<Long, TestLogReader.FlowCompletionTuple> flowCompletionTupleMap = TestLogReader.getFlowCompletionMapping("temp/test_n2_flow_threshold");

        // First round RTTs:
        // Flow #1: 1896
        // Flow #2: 848 + 1896
        // Flow #3: 2*848 + 1896

        // Second round RTTs:
        // When packet of #1 arrives again after 1048ns, the time it took to sent the other two packets, 848*2=1684ns,
        // is not yet expired. So the packet of #1 has to wait 1684-1048=648ns
        // Flow #1: 648+1896 (leaves queue at t=1896+648+848=3392)
        //
        // When packet of #2 arrives again after 1896+848=2744ns, queueing behind packet of #1 at t=2744. This means it
        // has to wait 3392-2744=648ns before starting its own transmission
        // Flow #2: 648+1896 (leaves queue at t=2744+648+848=4240)
        //
        // When packet of #3 arrives again after 2*848 + 1896 = 3592, queue behind packet of #2 at t=3592. This means it
        // has to wait 4240-3592=648ns before starting its own transmission
        // Flow #3: 648+1896 (leaves queue at t=3592+648+848=5088)

        // Third round RTTs:
        // When packet of #1 arrives again at t=1896+(1896+648)=4440, it will get behind the packet of #3, which is
        // transmitted at t=5088. It has thus to wait 648ns.
        // Flow #1: 648+1896 (leaves queue at t=4440+648+848=5936)
        //
        // When packet of #2 arrives again at t=(1896+848)+(1896+648)=5288, it will get behind the packet of #1, which is
        // transmitted at t=5936. It has thus to wait 648ns.
        // Flow #2: 648+1896 (leaves queue at t=5288+648+848=6784)
        // It will arrive back at 7832, at which the simulation is finished
        //
        // When packet of #3 arrives again at t=(1896+2*848)+(1896+648)=6136, it will get behind the packet of #2, which is
        // transmitted at t=6784. It has thus to wait 648ns.
        // Flow #3: 648+1896 (leaves queue at t=6136+648+848=7632)
        //
        // There is idle time of 7832-7632=200ns

        // Flow completion
        assertEquals(1896 + (1896 + 648) + (1896 + 648), flowCompletionTupleMap.get(0L).getEndTime());
        assertEquals((848 + 1896) + (1896 + 648) + (1896 + 648), flowCompletionTupleMap.get(1L).getEndTime());
        assertEquals((848 + 1896) + (1896 + 648) + (1896 + 648), flowCompletionTupleMap.get(2L).getEndTime());
        assertTrue(flowCompletionTupleMap.get(0L).isCompleted());
        assertTrue(flowCompletionTupleMap.get(1L).isCompleted());
        assertFalse(flowCompletionTupleMap.get(2L).isCompleted());

        // Port utilization
        assertEquals((7832.0 - 200.0) / 7832.0 * 100.0, portQueueStateTupleMap.get(new ImmutablePair<>(0, 1)).getUtilizationPercentage(), 1e-6);
        assertEquals(7832L - 200L, portQueueStateTupleMap.get(new ImmutablePair<>(0, 1)).getUtilizationNs());

    }

}
