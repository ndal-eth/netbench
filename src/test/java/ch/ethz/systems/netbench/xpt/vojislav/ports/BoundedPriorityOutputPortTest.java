package ch.ethz.systems.netbench.xpt.vojislav.ports;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.ext.basic.EcnTailDropOutputPort;
import ch.ethz.systems.netbench.ext.basic.EcnTailDropOutputPortGenerator;
import ch.ethz.systems.netbench.ext.basic.PerfectSimpleLink;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.ext.ecmp.EcmpSwitch;
import ch.ethz.systems.netbench.xpt.tcpbase.FullExtTcpPacket;
import ch.ethz.systems.netbench.xpt.voijslav.ports.BoundedPriorityOutputPort;

@RunWith(MockitoJUnitRunner.class)
public class BoundedPriorityOutputPortTest {

    private static final long packetSizeDataBytes = 1500;

    @Mock
    private NetworkDevice sourceNetworkDevice;

    @Mock
    private NetworkDevice targetNetworkDevice;

    @Mock
    private Link link;

    @Mock
    private TcpPacket packet;

    @Before
    public void setup() {
        Simulator.setup(0, new NBProperties(BaseAllowedProperties.LOG, BaseAllowedProperties.PROPERTIES_RUN));
        // Two network devices
        when(sourceNetworkDevice.getIdentifier()).thenReturn(1);
        when(targetNetworkDevice.getIdentifier()).thenReturn(2);
        // Port with 100 packets and 40 packets ECN limit
        when(link.getBandwidthBitPerNs()).thenReturn(10L);
        when(link.getDelayNs()).thenReturn(20L);
    }

    @After
    public void cleanup() {
        Simulator.reset();
    }

    // try to add 60 packets to the queue, but the queue limit is only 30 packets
    @Test
    public void testFields() {
    	// TCP header 480bit
    	// Ip header 480bit
    	// Packet size = 64bit
    	// Buffer size for 30 packets = (480+480+64)*30 = 30720 
    	long maxNumberOfBits = 30720;
    	long packetSizeByte = 8;
    	BoundedPriorityOutputPort port = new BoundedPriorityOutputPort(
    		sourceNetworkDevice,
    		targetNetworkDevice,
        	link,
        	maxNumberOfBits
        );
    	
    	Random r = new Random();
    	for(int i = 0; i < 60; i++){
    		FullExtTcpPacket p = new FullExtTcpPacket(
    			0L,packetSizeByte,0,0,1,0,0,0L,0L,false,
    			false,false,false,false,false,false,
    			false,false,0,r.nextInt(100)
    		);
    		port.enqueue(p);
    	}
    	assertEquals(port.getQueueSize(), 30);
    }

    
}
