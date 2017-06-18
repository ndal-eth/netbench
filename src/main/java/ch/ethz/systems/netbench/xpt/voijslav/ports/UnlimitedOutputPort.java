package ch.ethz.systems.netbench.xpt.voijslav.ports;


import ch.ethz.systems.netbench.core.network.Link;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.network.Packet;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * The unlimited output port employs a First-In-First-Out output
 * queue without limiting the size it can take.
 */
public class UnlimitedOutputPort extends OutputPort {

    public UnlimitedOutputPort(NetworkDevice ownNetworkDevice, NetworkDevice targetNetworkDevice, Link link) {
        super(ownNetworkDevice, targetNetworkDevice, link, new LinkedBlockingQueue<Packet>());
    }

    @Override
    public void enqueue(Packet packet) {
        guaranteedEnqueue(packet);
    }

}