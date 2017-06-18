package ch.ethz.systems.netbench.xpt.newreno.newrenotcp;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.run.traffic.FlowStartEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public class TcpResendTest {

    @Mock
    private NetworkDevice networkDeviceSender;

    @Mock
    private NetworkDevice networkDeviceReceiver;

    @Test
    public void testResendByTimeOut() throws IOException {


        // Create temporary run configuration file
        File tempRunConfig = File.createTempFile("temp-run-config", ".tmp");
        BufferedWriter runConfigWriter = new BufferedWriter(new FileWriter(tempRunConfig));
        runConfigWriter.write(
                "TCP_ROUND_TRIP_TIMEOUT_NS=7\n" +
                "TCP_MAX_WINDOW_SIZE=1000\n" +
                "TCP_MAX_SEGMENT_SIZE=100\n" +
                "TCP_INITIAL_SLOW_START_THRESHOLD=1000\n" +
                "TCP_INITIAL_WINDOW_SIZE=400\n" +
                "TCP_LOSS_WINDOW_SIZE=100\n" +
                "TCP_MINIMUM_SSTHRESH=200"
        );
        runConfigWriter.close();

        // Setup simulator
        Simulator.setup(1, new NBProperties(
                tempRunConfig.getAbsolutePath(),
                BaseAllowedProperties.LOG,
                BaseAllowedProperties.PROPERTIES_RUN,
                BaseAllowedProperties.EXPERIMENTAL
        ));

        // Packet captor
        ArgumentCaptor<Packet> senderOutgoingPacketCaptor = ArgumentCaptor.forClass(Packet.class);
        ArgumentCaptor<Packet> receiverOutgoingPacketCaptor = ArgumentCaptor.forClass(Packet.class);

        // Create the layers and attach mocked network devices
        final NewRenoTcpTransportLayer senderLayer = new NewRenoTcpTransportLayer(0);
        final NewRenoTcpTransportLayer receiverLayer = new NewRenoTcpTransportLayer(1);
        senderLayer.setNetworkDevice(networkDeviceSender);
        receiverLayer.setNetworkDevice(networkDeviceReceiver);

        // Forward all packets excepts the fourth (data 100-200) directly originating from sender to receiver
        final AtomicInteger counterSenderOut = new AtomicInteger(0);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Packet packet = (Packet) invocation.getArguments()[0];
                System.out.println("Sender sends out packet " + packet);
                int val = counterSenderOut.incrementAndGet();
                if (val != 4) {
                    receiverLayer.receive(packet);
                }
                return null;
            }
        }).when(networkDeviceSender).receiveFromTransportLayer(senderOutgoingPacketCaptor.capture());

        // Forward all packets directly originating from receiver to sender
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Packet packet = (Packet) invocation.getArguments()[0];
                System.out.println("Receiver sends out packet " + packet);
                senderLayer.receive(packet);
                return null;
            }
        }).when(networkDeviceReceiver).receiveFromTransportLayer(receiverOutgoingPacketCaptor.capture());

        // Start a flow from 0 to 1 of size <bytes>
        Simulator.registerEvent(new FlowStartEvent(0, senderLayer, 1, 700));

        // Run the simulator for a 7 nanoseconds to allow a resend to happen
        Simulator.runNs(7);

        // Check that the last packet is indeed the resent packet
        List<Packet> senderOutgoingPackets = senderOutgoingPacketCaptor.getAllValues();
        assertEquals(10, senderOutgoingPackets.size());
        // 1 (SYN) + 1 (ACK) + 3 (1-101, 101-201 (DROP), 201-301) + 2 (lim. transmit: 301-401, 401-501) + 1 (Fast retr. of 101-201)
        // 8th packet -> So index 7
        TcpSequenceTest.testPacket(senderOutgoingPackets.get(7), 101, 8935935, false, false, 100);

        // Clean-up
        Simulator.reset();


    }

}
