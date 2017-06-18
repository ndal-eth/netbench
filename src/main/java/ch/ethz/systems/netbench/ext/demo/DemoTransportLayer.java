package ch.ethz.systems.netbench.ext.demo;

import ch.ethz.systems.netbench.core.network.Socket;
import ch.ethz.systems.netbench.core.network.TransportLayer;

public class DemoTransportLayer extends TransportLayer {

    DemoTransportLayer(int identifier) {
        super(identifier);
    }

    @Override
    protected Socket createSocket(long flowId, int destinationId, long flowSizeByte) {
        return new DemoSocket(this, flowId, identifier, destinationId, flowSizeByte);
    }

}
