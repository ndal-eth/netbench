package ch.ethz.systems.netbench.xpt.voijslav.tcp.buffertcp;


import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.xpt.newreno.newrenotcp.NewRenoTcpSocket;
import ch.ethz.systems.netbench.xpt.tcpbase.FullExtTcpPacket;


public class BufferTcpSocket extends NewRenoTcpSocket {
	
	private final long maxBufferSize = 87380;
	private long realFlowSize;
	
	private long currentBufferSize;
    
	public BufferTcpSocket(
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
		
		realFlowSize = flowSizeByte;
		currentBufferSize = Math.min(maxBufferSize, realFlowSize);
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
    	long remainingData = Math.max(0, realFlowSize - sequenceNumber);
    	currentBufferSize =Math.max(0, currentBufferSize - dataSizeByte);
    	if(currentBufferSize <= maxBufferSize/2 && remainingData>0){
    		currentBufferSize = Math.min(currentBufferSize + remainingData, maxBufferSize);
    	}
    		
        return new FullExtTcpPacket(
            flowId, dataSizeByte, sourceId, destinationId,
            100, 80, 80, // TTL, source port, destination port
            sequenceNumber, ackNumber, // Seq number, Ack number
            false, false, ECE, // NS, CWR, ECE
            false, ACK, false, // URG, ACK, PSH
            false, SYN, false, // RST, SYN, FIN
            0, // Window size
            currentBufferSize
        );
    }

}
