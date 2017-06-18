package ch.ethz.systems.netbench.ext.flowlet;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IdentityFlowletIntermediaryTest {

    @Mock
    private TcpPacket packet;

    @Before
    public void setup() {
        Simulator.setup(0);
    }

    @After
    public void cleanup() {
        Simulator.reset();
    }

    @Test
    public void testIdentity() {

        // Outgoing adaptation
        IdentityFlowletIntermediary intermediary = new IdentityFlowletIntermediary();
        assertEquals(packet, intermediary.adaptOutgoing(packet));

        // Verify that the (src, dst, flow, flowlet)-hash generation is used
        verify(packet, times(1)).setHashSrcDstFlowFlowletDependent();
        verify(packet, times(1)).getFlowId();
        verifyNoMoreInteractions(packet);

        // Verify that nothing is being adapted for incoming packets
        assertEquals(packet, intermediary.adaptIncoming(packet));
        verifyNoMoreInteractions(packet);

    }

}
