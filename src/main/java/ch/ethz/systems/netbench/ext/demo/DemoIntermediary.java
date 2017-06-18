package ch.ethz.systems.netbench.ext.demo;

import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.Packet;

/**
 * The demonstrative basic identity intermediary does not match modify packets.
 */
public class DemoIntermediary extends Intermediary {

    DemoIntermediary() {
        super();
    }

    @Override
    public Packet adaptOutgoing(Packet packet) {
        return packet;
    }

    @Override
    public Packet adaptIncoming(Packet packet) {
        return packet;
    }

}
