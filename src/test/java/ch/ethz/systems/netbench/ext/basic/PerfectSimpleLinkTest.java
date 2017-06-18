package ch.ethz.systems.netbench.ext.basic;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(MockitoJUnitRunner.class)
public class PerfectSimpleLinkTest {

    @Mock
    private NetworkDevice networkDeviceA;

    @Mock
    private NetworkDevice networkDeviceB;

    @Test
    public void testInst() {
        PerfectSimpleLink link = new PerfectSimpleLink(77, 11);
        assertEquals(77, link.getDelayNs());
        assertEquals(11, link.getBandwidthBitPerNs());
        assertFalse(link.doesNextTransmissionFail(0));
    }

    @Test
    public void testGenerator() {
        Simulator.setup(0);
        PerfectSimpleLinkGenerator generator = new PerfectSimpleLinkGenerator(100, 200);
        PerfectSimpleLink link = (PerfectSimpleLink) generator.generate(networkDeviceA, networkDeviceB);
        assertEquals(100, link.getDelayNs());
        assertEquals(200, link.getBandwidthBitPerNs());
        Simulator.reset();
    }

}
