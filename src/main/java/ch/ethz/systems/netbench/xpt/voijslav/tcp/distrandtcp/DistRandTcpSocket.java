package ch.ethz.systems.netbench.xpt.voijslav.tcp.distrandtcp;

import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.xpt.newreno.newrenotcp.NewRenoTcpSocket;
import ch.ethz.systems.netbench.xpt.tcpbase.FullExtTcpPacket;
import ch.ethz.systems.netbench.xpt.voijslav.utility.FctDistributions;

public class DistRandTcpSocket extends NewRenoTcpSocket {

	private long predictedFlowSize;
	
	private int flowSizeDistribution(){
		double outcome = FctDistributions.rnd.nextDouble();
		return FctDistributions.flowSizeDistribution(outcome);
	}
	
    public DistRandTcpSocket(
    	TransportLayer transportLayer,
    	long flowId,
    	int sourceId,
    	int destinationId,
    	long flowSizeByte,
    	long seed
    ) {
		super(transportLayer, flowId, sourceId, destinationId, flowSizeByte);
		//6*(1200 + 96)*2*3
		this.roundTripTimeout = 23328L;
		this.congestionWindow = this.slowStartThreshold;
		//random value according to the distribution
		predictedFlowSize = flowSizeDistribution();
	}

    @Override
    protected FullExtTcpPacket createPacket(
        long dataSizeByte,
        long sequenceNumber,
        long ackNumber,
        boolean ACK,
        boolean SYN,
        boolean ECE
    ) {
    	long priority = Math.max(0,predictedFlowSize - sequenceNumber);
        return new FullExtTcpPacket(
            flowId, dataSizeByte, sourceId, destinationId,
            100, 80, 80, // TTL, source port, destination port
            sequenceNumber, ackNumber, // Seq number, Ack number
            false, false, ECE, // NS, CWR, ECE
            false, ACK, false, // URG, ACK, PSH
            false, SYN, false, // RST, SYN, FIN
            0, // Window size
            priority
        );
    }

}
