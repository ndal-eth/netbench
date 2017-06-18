package ch.ethz.systems.netbench.core.run.traffic;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FlowStartEventTest {

    @Before
    public void setup() {
        Simulator.setup(0);
    }

    @After
    public void cleanup() {
        Simulator.reset();
    }

    @Test
    public void testTriggerIsolated() {
        TransportLayer transportLayer = mock(TransportLayer.class);
        FlowStartEvent event = new FlowStartEvent(1000, transportLayer, 98, 100000);
        event.trigger();
        verify(transportLayer, times(1)).startFlow(98, 100000);
    }

    @Test
    public void testTriggerInSimulation() {
        TransportLayer transportLayer = mock(TransportLayer.class);
        FlowStartEvent event = new FlowStartEvent(1000, transportLayer, 23, 56737);
        Simulator.registerEvent(event);
        Simulator.runNs(2000);
        verify(transportLayer, times(1)).startFlow(23, 56737);
    }

    @Test
    public void testTriggerInSimulationJustNot() {
        TransportLayer transportLayer = mock(TransportLayer.class);
        FlowStartEvent event = new FlowStartEvent(1000, transportLayer, 98, 100000);
        Simulator.registerEvent(event);
        Simulator.runNs(999);
        verify(transportLayer, times(0)).startFlow(98, 100000);
    }

    @Test
    public void testTriggerInSimulationJust() {
        TransportLayer transportLayer = mock(TransportLayer.class);
        FlowStartEvent event = new FlowStartEvent(999, transportLayer, 98, 100000);
        Simulator.registerEvent(event);
        Simulator.runNs(999);
        verify(transportLayer, times(1)).startFlow(98, 100000);
    }

    @Test
    public void testToString() {
        TransportLayer transportLayer = mock(TransportLayer.class);
        FlowStartEvent event = new FlowStartEvent(999, transportLayer, 98, 100000);
        event.toString();
    }

}
