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

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class DemoRunParallel8Test {

    @Test
    public void testDemoRunP8() throws IOException {

        // Perform run (run folder: temp/test_parallel_n8)
        String[] args = new String[]{"example/runs/test_parallel_n8.properties"};
        MainFromProperties.main(args);

        // Fetch log mappings
        Map<Pair<Integer, Integer>, TestLogReader.PortUtilizationTuple> portQueueStateTupleMap = TestLogReader.getPortUtilizationMapping("temp/test_parallel_n8");
        Map<Long, TestLogReader.FlowCompletionTuple> flowCompletionTupleMap = TestLogReader.getFlowCompletionMapping("temp/test_parallel_n8");

        // Flow completion
        for (long i = 0; i < 4; i++) {

            // Flow completion
            TestLogReader.FlowCompletionTuple tuple = flowCompletionTupleMap.get(i);
            assertEquals(1490129, tuple.getEndTime());
            assertEquals(i * 2, tuple.getSourceId());
            assertEquals(i * 2 + 1, tuple.getTargetId());
            assertTrue(tuple.isCompleted());
            assertEquals(1490129, tuple.getDuration());
            assertEquals(0, tuple.getStartTime());
            assertEquals(785842, tuple.getTotalSizeBytes());
            assertEquals(785842, tuple.getSentBytes());

            // Data link
            assertEquals((785.0*848.0 + 721.0) / 1e9 * 100.0, portQueueStateTupleMap.get(new ImmutablePair<>(((int) i)*2, ((int) i)*2+1)).getUtilizationPercentage(), 1e-6);
            assertEquals((785*848 + 721), portQueueStateTupleMap.get(new ImmutablePair<>(0, 1)).getUtilizationNs());

            // Ack link
            assertEquals((786.0*48.0) / 1e9 * 100.0, portQueueStateTupleMap.get(new ImmutablePair<>(((int) i)*2+1, ((int) i)*2)).getUtilizationPercentage(), 1e-6);
            assertEquals((786*48), portQueueStateTupleMap.get(new ImmutablePair<>(1, 0)).getUtilizationNs());

        }

    }

}
