package ch.ethz.systems.netbench.xpt.newreno.newrenotcp;

import ch.ethz.systems.netbench.core.network.Socket;
import ch.ethz.systems.netbench.core.network.TransportLayer;

public class NewRenoTcpTransportLayer extends TransportLayer {

    /**
     * Create the TCP transport layer with the given network device identifier.
     * The network device identifier is used to create unique flow identifiers.
     *
     * @param identifier        Parent network device identifier
     */
    public NewRenoTcpTransportLayer(int identifier) {
        super(identifier);
    }

    @Override
    protected Socket createSocket(long flowId, int destinationId, long flowSizeByte) {
        return new NewRenoTcpSocket(this, flowId, this.identifier, destinationId, flowSizeByte);
    }

}
