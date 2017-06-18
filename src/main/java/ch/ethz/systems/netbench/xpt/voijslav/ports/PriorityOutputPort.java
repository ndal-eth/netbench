package ch.ethz.systems.netbench.xpt.voijslav.ports;


import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;
import ch.ethz.systems.netbench.xpt.tcpbase.PriorityHeader;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;


/**
 * The priority output port inserts packets into
 * the out queue based on their priority field.
 * Initial
 */
public class PriorityOutputPort extends OutputPort {

    private static final int INITIAL_QUEUE_CAPACITY = 100;

    public PriorityOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link) {
        super(ownNetworkDevice, targetNetworkDevice, link, new PriorityBlockingQueue<>(INITIAL_QUEUE_CAPACITY, new Comparator<Packet>() {
            @Override
            public int compare(Packet o1, Packet o2) {
                if(o1 instanceof TcpPacket && o2 instanceof TcpPacket){

                    // Cast
                    PriorityHeader tcp1 = (PriorityHeader) o1;
                    PriorityHeader tcp2 = (PriorityHeader) o2;

                    // First compare based on priority
                    int res = Long.compare(tcp1.getPriority(), tcp2.getPriority());

                    // If packets have same priorities, compare based on departure time
                    if (res == 0 ){
                        res = Long.compare(tcp1.getDepartureTime(), tcp2.getDepartureTime());
                    }

                    return res;
                }
                return 0;
            }
        }));
    }

    @Override
    public void enqueue(Packet packet) {
        guaranteedEnqueue(packet);
    }

}
