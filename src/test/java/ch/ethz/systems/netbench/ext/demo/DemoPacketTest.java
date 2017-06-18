package ch.ethz.systems.netbench.ext.demo;

import ch.ethz.systems.netbench.core.Simulator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class DemoPacketTest {

    @Test
    public void testFields() {

        Simulator.setup(0);

        // Create packet
        DemoPacket packet = new DemoPacket(
                283, 1002,
                11, 12,
                99, 77
        );

        // Basic header fields
        assertEquals(283, packet.getFlowId());
        assertEquals(1002, packet.getDataSizeByte());
        assertEquals(11, packet.getSourceId());
        assertEquals(12, packet.getDestinationId());
        assertEquals(99, packet.getTTL());
        assertEquals(77, packet.getAckSizeByte());
        assertEquals((1002 + 60) * 8, packet.getSizeBit());

        // Time-to-live
        for (int i = 0; i < 98; i++) {
            assertFalse(packet.decrementTtlAndIsDead());
            assertEquals(99 - i - 1, packet.getTTL());
        }
        assertTrue(packet.decrementTtlAndIsDead());
        assertEquals(0, packet.getTTL());

        // ECN mark
        assertFalse(packet.getECN());
        packet.markCongestionEncountered();
        assertTrue(packet.getECN());
        packet.markCongestionEncountered();
        assertTrue(packet.getECN());

        // Departure time
        assertEquals(0, packet.getDepartureTime());

        // String representation
        assertNotNull(packet.toString());

        Simulator.reset();


    }

}
