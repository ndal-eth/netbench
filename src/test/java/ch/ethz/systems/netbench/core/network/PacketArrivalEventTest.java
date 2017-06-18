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
public class PacketArrivalEventTest {

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
        NetworkDevice networkDevice = mock(NetworkDevice.class);
        PacketArrivalEvent event = new PacketArrivalEvent(1000, packet, networkDevice);
        event.trigger();
        verify(networkDevice, times(1)).receive(packet);
    }

    @Test
    public void testTriggerInSimulation() {
        NetworkDevice networkDevice = mock(NetworkDevice.class);
        PacketArrivalEvent event = new PacketArrivalEvent(1000, packet, networkDevice);
        Simulator.registerEvent(event);
        Simulator.runNs(2000);
        verify(networkDevice, times(1)).receive(packet);
    }

    @Test
    public void testTriggerInSimulationJustNot() {
        NetworkDevice networkDevice = mock(NetworkDevice.class);
        PacketArrivalEvent event = new PacketArrivalEvent(1000, packet, networkDevice);
        Simulator.registerEvent(event);
        Simulator.runNs(999);
        verify(networkDevice, times(0)).receive(packet);
    }

    @Test
    public void testTriggerInSimulationJust() {
        NetworkDevice networkDevice = mock(NetworkDevice.class);
        PacketArrivalEvent event = new PacketArrivalEvent(999, packet, networkDevice);
        Simulator.registerEvent(event);
        Simulator.runNs(999);
        verify(networkDevice, times(1)).receive(packet);
    }

    @Test
    public void testToString() {
        NetworkDevice networkDevice = mock(NetworkDevice.class);
        PacketArrivalEvent event = new PacketArrivalEvent(999, packet, networkDevice);
        event.toString();
    }

}
