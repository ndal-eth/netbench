package ch.ethz.systems.netbench.ext.flowlet;

import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.ext.basic.TcpHeader;

/**
 * The identity flowlet intermediary only sets a single flowlet
 * for its entire duration (effectively, has no effect).
 *
 * A single flow is only sent on a single path, its packets hash is constant.
 */
public class IdentityFlowletIntermediary extends FlowletIntermediary {

    public IdentityFlowletIntermediary() {
        super();
    }

    @Override
    public Packet adaptOutgoing(Packet packet) {

        // Log packet burst interval
        long lastSent = getAndUpdateLastSent(packet.getFlowId());
        // TODO: SimulationLogger.logPacketBurstGap(Simulator.getCurrentTime() - lastSent);

        // Set the hash dependent on flow and flowlet, although only the
        // flow changes (flowlet remains 0 always)
        TcpHeader tcpHeader = (TcpHeader) packet;
        tcpHeader.setHashSrcDstFlowFlowletDependent();

        return packet;
    }

    @Override
    public Packet adaptIncoming(Packet packet) {
        return packet;
    }

}
