package ch.ethz.systems.netbench.xpt.simple.simpletcp;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.BaseAllowedProperties;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public class SimpleTcpSequenceTest {

    @Mock
    private NetworkDevice networkDeviceSender;

    @Mock
    private NetworkDevice networkDeviceReceiver;

    @Test
    public void testVariousPacketSequences() throws IOException {

        // NOTE: this test used to be here to evaluate whether the loop-around
        // of acknowledgment numbers worked. As this is deprecated, what is tested
        // here is only the normal succession of acknowledgement numbers (totalSeqNumber is set very large, 1TB)

        // Case A: does not require loops
        testPacketSequences(100, 11, 49, 133);

        // Case B: requires loops
        testPacketSequences(100, 11, 49, 1003);

        // Case C: requires loop just
        testPacketSequences(100, 10, 50, 1000);

        // Segment size of one
        testPacketSequences(1, 1, 1, 34);

        // Edge case for what is allowed with segment and window size
        testPacketSequences(37, 37, 37, 343);

    }

    public void testPacketSequences(int maxWindowSize, int maxSegmentSize, int initialWindowSize, long flowSizeByte) throws IOException {


        // 1TB
        long totalSeqNumber = 1000000000000L;

        // Create temporary run configuration file
        File tempRunConfig = File.createTempFile("temp-run-config", ".tmp");
        BufferedWriter runConfigWriter = new BufferedWriter(new FileWriter(tempRunConfig));
        runConfigWriter.write(
                "TCP_ROUND_TRIP_TIMEOUT_NS=1\n" +
                "TCP_MAX_WINDOW_SIZE=" + maxWindowSize + "\n" +
                "TCP_MAX_SEGMENT_SIZE=" + maxSegmentSize + "\n" +
                "TCP_INITIAL_SLOW_START_THRESHOLD=0\n" +
                "TCP_INITIAL_WINDOW_SIZE=" + initialWindowSize + "\n"
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
        final SimpleTcpTransportLayer senderLayer = new SimpleTcpTransportLayer(0);
        final SimpleTcpTransportLayer receiverLayer = new SimpleTcpTransportLayer(1);
        senderLayer.setNetworkDevice(networkDeviceSender);
        receiverLayer.setNetworkDevice(networkDeviceReceiver);

        // Forward all packets directly originating from sender to receiver
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Packet packet = (Packet) invocation.getArguments()[0];
                System.out.println("Sender sends out packet " + packet);
                receiverLayer.receive(packet);
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
        senderLayer.startFlow(1, flowSizeByte);

        ////////////////////////////////
        // Sender outgoing packets check
        //
        List<Packet> senderOutgoingPackets = senderOutgoingPacketCaptor.getAllValues();
        assertEquals(2 + (int) Math.ceil((double) flowSizeByte / (double) maxSegmentSize), senderOutgoingPackets.size());

        // TCP-handshake: (1) SYN
        testPacket(senderOutgoingPackets.get(0), 0, 27587892579L, false, true, 0);

        // TCP-handshake: (3) ACK
        testPacket(senderOutgoingPackets.get(1), 1, 1, true, false, 0);

        // Data packets from the sender to the receiver
        for (int i = 2; i < senderOutgoingPackets.size(); i++) {
            testPacket(
                    senderOutgoingPackets.get(i),
                    (1 + ((i - 2) * maxSegmentSize)) % totalSeqNumber,
                    49369896833L,
                    false,
                    false,
                    (i == senderOutgoingPackets.size() - 1) ? flowSizeByte - (senderOutgoingPackets.size() - 3) * maxSegmentSize : maxSegmentSize
            );
        }

        ////////////////////////////////
        // Receiver outgoing packets check
        //
        List<Packet> receiverOutgoingPackets = receiverOutgoingPacketCaptor.getAllValues();
        assertEquals(1 + (int) Math.ceil((double) flowSizeByte / (double) maxSegmentSize), receiverOutgoingPackets.size());

        // TCP-handshake: (2) SYN+ACK
        testPacket(receiverOutgoingPackets.get(0), 0, 1, true, true, 0);

        // Ack packets from the receiver to the sender
        for (int i = 2; i < receiverOutgoingPackets.size(); i++) {
            testPacket(
                    receiverOutgoingPackets.get(i),
                    1,
                    (i == receiverOutgoingPackets.size() - 1) ? (1 + flowSizeByte) % totalSeqNumber : (1 + i * maxSegmentSize) % totalSeqNumber,
                    true,
                    false,
                    0
            );
        }

        // Clean-up
        Simulator.reset();


    }

    /**
     * Test whether the packet has the given sequence information.
     *
     * @param genPacket     Generic packet instance
     * @param seqNumber     Expected sequence number
     * @param ackNumber     Expected acknowledgment number
     * @param isAck         Whether it is expected to be an acknowledgement
     * @param isSyn         Whether it is expected to be a synchronization packet
     * @param dataSizeByte  Expected packet carried data size in bytes
     */
    public static void testPacket(Packet genPacket, long seqNumber, long ackNumber, boolean isAck, boolean isSyn, long dataSizeByte) {
        TcpPacket packet = (TcpPacket) genPacket;
        assertEquals(seqNumber, packet.getSequenceNumber());
        if (packet.isACK()) {
            assertEquals(ackNumber, packet.getAcknowledgementNumber());
        }
        assertEquals(isAck, packet.isACK());
        assertEquals(isSyn, packet.isSYN());
        assertEquals(dataSizeByte, packet.getDataSizeByte());
    }

}
