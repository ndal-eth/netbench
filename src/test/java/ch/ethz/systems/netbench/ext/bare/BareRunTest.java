package ch.ethz.systems.netbench.ext.bare;

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
public class BareRunTest {

    @Test
    public void testBareRun() throws IOException {

        // Perform run (run folder: temp/test_n2)
        String[] args = new String[]{"example/runs/test_n2_bare.properties"};
        MainFromProperties.main(args);

        // Fetch log mappings
        Map<Pair<Integer, Integer>, TestLogReader.PortUtilizationTuple> portQueueStateTupleMap = TestLogReader.getPortUtilizationMapping("temp/test_n2_bare");
        Map<Long, TestLogReader.FlowCompletionTuple> flowCompletionTupleMap = TestLogReader.getFlowCompletionMapping("temp/test_n2_bare");

        // Flow completion
        TestLogReader.FlowCompletionTuple tuple = flowCompletionTupleMap.get(0L);
        assertEquals(50000, tuple.getEndTime());
        assertFalse(tuple.isCompleted());
        assertEquals(50000, tuple.getDuration());
        assertEquals(55200, tuple.getSentBytes());
        assertEquals(1000000000L, tuple.getTotalSizeBytes());

        // Sent one pa

        // Port utilization

        // 0 -> 1
        assertEquals(100.0, portQueueStateTupleMap.get(new ImmutablePair<>(0, 1)).getUtilizationPercentage(), 1e-6);
        assertEquals(50000, portQueueStateTupleMap.get(new ImmutablePair<>(0, 1)).getUtilizationNs());

        // 1 -> 0
        //assertEquals((5*48) / 9479.0 * 100.0, portQueueStateTupleMap.get(new Pair<>(1, 0)).getUtilizationPercentage(), 1e-6);
        //assertEquals((5*48), portQueueStateTupleMap.get(new Pair<>(1, 0)).getUtilizationNs());

    }

}
