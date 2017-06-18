package ch.ethz.systems.netbench.xpt.sourcerouting;

import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SourceRoutingEncapsulationTest {

    @Mock
    private TcpPacket packet;

    @Test
    public void simpleInstOneHop() {

        // Create a path
        SourceRoutingPath path = new SourceRoutingPath();
        path.add(3);
        path.add(12);

        when(packet.getSizeBit()).thenReturn((long) (3535 * 8 + 60 * 8 + 60 * 8));
        when(packet.getSourceId()).thenReturn(46);
        when(packet.getDestinationId()).thenReturn(88);
        when(packet.getFlowId()).thenReturn(577L);

        // Create packet
        SourceRoutingEncapsulation encapsulation = new SourceRoutingEncapsulation(
                packet, path
        );

        // Getters
        assertEquals(packet, encapsulation.getPacket());
        assertEquals(12, encapsulation.nextHop());

        // Congestion mark propagation
        encapsulation.markCongestionEncountered();
        verify(packet, times(1)).markCongestionEncountered();

        // Does not increase packet size
        assertEquals(packet.getSizeBit(), encapsulation.getSizeBit());

        // These fields are necessary to be the same as the packet
        assertEquals(577L, encapsulation.getFlowId());
        assertEquals(46, encapsulation.getSourceId());
        assertEquals(88, encapsulation.getDestinationId());

    }

    @Test
    public void simpleInstThreeHop() {

        // Create a path
        SourceRoutingPath path = new SourceRoutingPath();
        path.add(3);
        path.add(8);
        path.add(133);
        path.add(12);

        // Create packet
        SourceRoutingEncapsulation encapsulation = new SourceRoutingEncapsulation(
                packet, path
        );

        // Getters
        assertEquals(packet, encapsulation.getPacket());
        assertEquals(8, encapsulation.nextHop());
        assertEquals(133, encapsulation.nextHop());
        assertEquals(12, encapsulation.nextHop());

        // Congestion mark propagation
        encapsulation.markCongestionEncountered();
        verify(packet, times(1)).markCongestionEncountered();

    }

}
