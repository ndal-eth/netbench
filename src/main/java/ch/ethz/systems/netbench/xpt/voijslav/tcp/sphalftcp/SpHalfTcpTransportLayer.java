package ch.ethz.systems.netbench.xpt.voijslav.tcp.sphalftcp;

import ch.ethz.systems.netbench.core.network.Socket;
import ch.ethz.systems.netbench.core.network.TransportLayer;

public class SpHalfTcpTransportLayer extends TransportLayer {

    /**
     * Create the TCP transport layer with the given network device identifier.
     * The network device identifier is used to create unique flow identifiers.
     *
     * @param identifier        Parent network device identifier
     */
    public SpHalfTcpTransportLayer(int identifier) {
        super(identifier);
    }

    @Override
    protected Socket createSocket(long flowId, int destinationId, long flowSizeByte) {
        return new SpHalfTcpSocket(this, flowId, this.identifier, destinationId, flowSizeByte);
    }

}
