package ch.ethz.systems.netbench.xpt.voijslav.ports;

import java.util.Comparator;

import com.google.common.collect.MinMaxPriorityQueue;

import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.tcpbase.PriorityHeader;

public class BoundedPriorityOutputPort extends OutputPort {

    private MinMaxPriorityQueue<Packet> priorityQueue;
    private long maxQueueSizeInBits;

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public BoundedPriorityOutputPort(
            NetworkDevice ownNetworkDevice,
            NetworkDevice targetNetworkDevice,
            Link link,
            long maxQueueSizeInBits
    ) {
        super(ownNetworkDevice, targetNetworkDevice, link, MinMaxPriorityQueue.orderedBy(new Comparator<Packet>() {

            @Override
            public int compare(Packet o1, Packet o2) {
                if(o1 instanceof TcpPacket && o2 instanceof TcpPacket){

                    // Cast packets
                    PriorityHeader tcp1 = (PriorityHeader) o1;
                    PriorityHeader tcp2 = (PriorityHeader) o2;

                    // Compare first on priority, then on departure time
                    int res = Long.compare(tcp1.getPriority(), tcp2.getPriority());
                    if (res == 0) {
                        res = Long.compare(tcp1.getDepartureTime(), tcp2.getDepartureTime());
                    }

                    return res;

                }
                return 0;
            }

        }).create());
        this.maxQueueSizeInBits = maxQueueSizeInBits;
        this.priorityQueue = (MinMaxPriorityQueue) getQueue();
    }

    @Override
    public void enqueue(Packet packet) {
        guaranteedEnqueue(packet);
        while (getBufferOccupiedBits() > maxQueueSizeInBits){
        	decreaseBufferOccupiedBits(priorityQueue.pollLast().getSizeBit());
        }
        
    }

}
