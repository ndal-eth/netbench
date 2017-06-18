package ch.ethz.systems.netbench.xpt.voijslav.tcp.sparktcp;


import ch.ethz.systems.netbench.core.network.Socket;
import ch.ethz.systems.netbench.core.network.TransportLayer;

public class SparkTransportLayer extends TransportLayer {

	private long seed;
    /**
     * Create the TCP transport layer with the given network device identifier.
     * The network device identifier is used to create unique flow identifiers.
     *
     * @param identifier        Parent network device identifier
     */
    public SparkTransportLayer(int identifier, long seed) {
        super(identifier);
        this.seed = seed;
    }

    @Override
    protected Socket createSocket(long flowId, int destinationId, long flowSizeByte) {
        return new SparkSocket(this, flowId, this.identifier, destinationId, flowSizeByte, seed);
    }

}
