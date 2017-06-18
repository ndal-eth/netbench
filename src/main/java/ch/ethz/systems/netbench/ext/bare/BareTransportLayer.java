package ch.ethz.systems.netbench.ext.bare;

import ch.ethz.systems.netbench.core.network.Socket;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.demo.DemoSocket;

public class BareTransportLayer extends TransportLayer {

    BareTransportLayer(int identifier) {
        super(identifier);
    }

    @Override
    protected Socket createSocket(long flowId, int destinationId, long flowSizeByte) {
        return new BareSocket(this, flowId, identifier, destinationId, flowSizeByte);
    }

}
