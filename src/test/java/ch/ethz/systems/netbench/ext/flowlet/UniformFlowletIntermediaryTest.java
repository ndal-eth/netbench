package ch.ethz.systems.netbench.ext.flowlet;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UniformFlowletIntermediaryTest {

    @Mock
    private TcpPacket packet;

    @Before
    public void setup() {
        NBProperties runConfiguration = new NBProperties(BaseAllowedProperties.PROPERTIES_RUN, BaseAllowedProperties.LOG, BaseAllowedProperties.EXTENSION);
        runConfiguration.overrideProperty("FLOWLET_GAP_NS", "1000");
        Simulator.setup(0, runConfiguration);
    }

    @After
    public void cleanup() {
        Simulator.reset();
    }

    @Test
    public void testThreePointsInTime() {

        // Retrieve default gap
        long gap = Simulator.getConfiguration().getLongPropertyOrFail("FLOWLET_GAP_NS");
        assertTrue(gap > 0);

        // Mock packet belonging to flow id 100
        when(packet.getFlowId()).thenReturn(100L);

        // Create intermediary
        UniformFlowletIntermediary intermediary = new UniformFlowletIntermediary();

        // Receive packets at three different
        // points in time
        intermediary.adaptOutgoing(packet); // Receive 1st packet at t=0
        Simulator.runNs(gap - 1);
        intermediary.adaptOutgoing(packet); // Receive 2nd packet at t=FLOWLET_GAP-1
        Simulator.runNs(2 * gap - 1);
        intermediary.adaptOutgoing(packet); // Receive 3rd packet at t=2*FLOWLET_GAP+1

        // Check flowlet identifiers at the three points in time
        ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.class);
        verify(packet, times(3)).setFlowletId(captor.capture());
        assertEquals(0, (int) captor.getAllValues().get(0)); // No time has passed on a non-zero gap, so flowlet id should still be 0
        assertEquals(0, (int) captor.getAllValues().get(1)); // Just not enough time has passed
        assertEquals(1, (int) captor.getAllValues().get(2)); // Exactly enough time has passed

        // Must use the correct hash setting
        verify(packet, times(3)).setHashSrcDstFlowFlowletDependent();
        verify(packet, times(0)).setHashSrcDstFlowletDependent();

    }

    @Test
    public void testIdentityIncoming() {
        UniformFlowletIntermediary intermediary = new UniformFlowletIntermediary();
        assertEquals(packet, intermediary.adaptIncoming(packet));
        verifyZeroInteractions(packet);
    }

}
