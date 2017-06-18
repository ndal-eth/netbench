package ch.ethz.systems.netbench.core.network;

import ch.ethz.systems.netbench.core.Simulator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PacketDispatchedEventTest {

    @Mock
    private Packet packet;

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
        OutputPort port = mock(OutputPort.class);
        PacketDispatchedEvent event = new PacketDispatchedEvent(1000, packet, port);
        event.trigger();
        verify(port, times(1)).dispatch(packet);
    }

    @Test
    public void testTriggerInSimulation() {
        OutputPort port = mock(OutputPort.class);
        PacketDispatchedEvent event = new PacketDispatchedEvent(1000, packet, port);
        Simulator.registerEvent(event);
        Simulator.runNs(2000);
        verify(port, times(1)).dispatch(packet);
    }

    @Test
    public void testTriggerInSimulationJustNot() {
        OutputPort port = mock(OutputPort.class);
        PacketDispatchedEvent event = new PacketDispatchedEvent(1000, packet, port);
        Simulator.registerEvent(event);
        Simulator.runNs(999);
        verify(port, times(0)).dispatch(packet);
    }

    @Test
    public void testTriggerInSimulationJust() {
        OutputPort port = mock(OutputPort.class);
        PacketDispatchedEvent event = new PacketDispatchedEvent(999, packet, port);
        Simulator.registerEvent(event);
        Simulator.runNs(999);
        verify(port, times(1)).dispatch(packet);
    }

    @Test
    public void testToString() {
        OutputPort port = mock(OutputPort.class);
        PacketDispatchedEvent event = new PacketDispatchedEvent(999, packet, port);
        event.toString();
    }

}
