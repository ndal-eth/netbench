package ch.ethz.systems.netbench.core.network;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.run.traffic.FlowStartEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.PriorityQueue;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class EventTest {

    @Before
    public void setup() {
        Simulator.setup(0);
    }

    @After
    public void cleanup() {
        Simulator.reset();
    }

    @Test
    public void testQueueOrder() {

        // Events
        Event e1 = new FlowStartEvent(1000, null, 0, 0);
        Event e2 = new FlowStartEvent(0, null, 0, 0);
        Event e3 = new FlowStartEvent(848, null, 0, 0);
        Event e4 = new FlowStartEvent(1000, null, 0, 0);
        Event e5 = new FlowStartEvent(999999, null, 0, 0);

        // Create queue
        PriorityQueue<Event> queue = new PriorityQueue<>();
        queue.add(e1);
        queue.add(e2);
        queue.add(e3);
        queue.add(e4);
        queue.add(e5);

        // Empty queue and make sure that it is in correct order
        assertEquals(queue.size(), 5);
        assertEquals(queue.peek().getTime(), 0);
        assertEquals(queue.poll().getTime(), 0);
        assertEquals(queue.size(), 4);
        assertEquals(queue.poll().getTime(), 848);
        assertEquals(queue.size(), 3);
        assertEquals(queue.poll().getTime(), 1000);
        assertEquals(queue.size(), 2);
        assertEquals(queue.poll().getTime(), 1000);
        assertEquals(queue.size(), 1);
        assertEquals(queue.poll().getTime(), 999999);
        assertEquals(queue.size(), 0);
        assertEquals(null, queue.poll());

    }

}
