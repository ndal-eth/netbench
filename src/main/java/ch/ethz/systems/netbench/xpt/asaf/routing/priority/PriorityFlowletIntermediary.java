package ch.ethz.systems.netbench.xpt.asaf.routing.priority;

import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.ext.basic.TcpHeader;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.ext.flowlet.FixedGapFlowletIntermediary;
import ch.ethz.systems.netbench.xpt.tcpbase.PriorityHeader;

import java.util.HashMap;

/**
 * This is used to add priority to packets, when a flowlet gap
 * is encountered not only do we change paths (as the UniformIntermediary)
 * but we also modify this flow as lower priority.
 * 
 * This translates to:
 * 	1) Set flow id as low priority
 * 	2) Measure the amount of current window size
 * 	3) If we've sent the current window size without interuptions, bring back the 
 * 		regular priority
 * 	4) if not we reset the count
 *
 */
public class PriorityFlowletIntermediary extends FixedGapFlowletIntermediary {

	private HashMap<Long, Long> lowPriorityFlows = new HashMap<Long, Long>();
	
    PriorityFlowletIntermediary() {
        super();
    }

    /**
     * Adapt the flowlet in the outgoing packet to the correct one for the
     * flow it is in.
     *
     * @param packet     Packet instance
     *
     * @return  Packet with adjusted flowlet identifier (e.g. used in the ecmpHash() function of a TCP packet)
     */
    @Override
    public Packet adaptOutgoing(Packet packet) {

    		// Retrieve flow to which the packet belongs
        long flowId = packet.getFlowId();

        // Retrieve current flowlet of the flow
        int currentFlowlet = getCurrentFlowlet(flowId);

        // If the flowlet gap is exceeded, go to next flowlet
        if (flowletGapExceeded(flowId)) {
            currentFlowlet = Math.max(0, currentFlowlet + 1);
            setCurrentFlowlet(flowId, currentFlowlet);
            
            // Mark that this flow is now in lower priority
            this.lowPriorityFlows.put(flowId, (long)((TcpPacket)packet).getWindowSize());
        } 
        
        if (this.lowPriorityFlows.containsKey(flowId)) {

        		// check if we finished sending the congestion window
                PriorityHeader tcpPacket = (PriorityHeader) packet;
        		long remainingToSend = this.lowPriorityFlows.get(flowId);
        		
    			this.lowPriorityFlows.put(flowId, remainingToSend - tcpPacket.getDataSizeByte());
    			tcpPacket.increasePriority();
    			
    			if (this.lowPriorityFlows.get(flowId) <= 0){
    				this.lowPriorityFlows.remove(flowId);
    			}
        }

        // Actually set flowlet identifier on the packet
        packet.setFlowletId(currentFlowlet);

        // Set the actual hash to be dependent on both flow id and flowlet identifier
        TcpHeader tcpHeader = (TcpHeader) packet;
        tcpHeader.setHashSrcDstFlowFlowletDependent();

        // Pass on to regular packet handling
        return packet;

    }

    /**
     * Identity.
     *
     * @param packet    Packet instance
     *
     * @return  Packet instance
     */
    @Override
    public Packet adaptIncoming(Packet packet) {
        return packet;
    }

}
