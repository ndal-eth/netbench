package ch.ethz.systems.netbench.ext.valiant;

import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.tcpbase.FullExtTcpPacket;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ValiantEncapsulationTest {

    @Mock
    private TcpPacket packet;

    @Test
    public void simpleInstFakePacket() {

        when(packet.getSizeBit()).thenReturn((long) (3535 * 8 + 60 * 8 + 60 * 8));
        when(packet.getSourceId()).thenReturn(46);
        when(packet.getDestinationId()).thenReturn(88);
        when(packet.getFlowId()).thenReturn(577L);

        // Create packet
        ValiantEncapsulation encapsulation = new ValiantEncapsulation(
                packet, 5
        );

        // Getters
        Assert.assertEquals(packet, encapsulation.getPacket());
        assertEquals(5, encapsulation.getValiantDestination());

        // Passing valiant
        assertFalse(encapsulation.passedValiant());
        encapsulation.markPassedValiant();
        assertTrue(encapsulation.passedValiant());

        // Congestion mark propagation
        encapsulation.markCongestionEncountered();
        verify(packet, times(1)).markCongestionEncountered();

        // Does not increase packet size
        Assert.assertEquals(packet.getSizeBit(), encapsulation.getSizeBit());

        // These fields are necessary to be the same as the packet
        Assert.assertEquals(577L, encapsulation.getFlowId());
        Assert.assertEquals(46, encapsulation.getSourceId());
        Assert.assertEquals(88, encapsulation.getDestinationId());

    }

    @Test
    public void simpleInstRealPacket() {

        // Create packet
        TcpPacket packet = new FullExtTcpPacket(
                46622, 1380, 67, 89,
                100, 80, 81, // TTL, source port, destination port
                473737, 373, // Seq number, Ack number
                false, false, false, false, true, false, false, false, false,
                // NS  CWR    ECE    URG    ACK   PSH    RST    SYN    FIN
                4262, 0 // Window size
        );

        // Create packet
        ValiantEncapsulation encapsulation = new ValiantEncapsulation(
                packet, 5
        );

        // Does not increase packet size
        Assert.assertEquals(packet.getSizeBit(), encapsulation.getSizeBit());


    }

}
