package ch.ethz.systems.netbench.xpt.sourcerouting;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.network.*;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;

import java.util.ArrayList;
import java.util.List;

public class SourceRoutingSwitch extends NetworkDevice {

    // Routing table
    private List<List<SourceRoutingPath>> destinationToPaths;

    private boolean isWithinExtendedTopology;

    /**
     * Constructor for Source Routing switch WITH a transport layer attached to it.
     *
     * @param identifier            Network device identifier
     * @param transportLayer        Underlying server transport layer instance (set null, if none)
     * @param n                     Number of network devices in the entire network (for routing table size)
     * @param intermediary          Flowlet intermediary instance (takes care of hash adaptation for flowlet support)
     */
    SourceRoutingSwitch(int identifier, TransportLayer transportLayer, int n, Intermediary intermediary) {
        super(identifier, transportLayer, intermediary);
        this.destinationToPaths = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            this.destinationToPaths.add(new ArrayList<>());
        }
        isWithinExtendedTopology = Simulator.getConfiguration().getGraphDetails().isAutoExtended();
    }

    @Override
    public void receive(Packet genericPacket) {

        // Convert to encapsulation
        SourceRoutingEncapsulation encapsulation = (SourceRoutingEncapsulation) genericPacket;

        // Check if it has arrived
        if (encapsulation.getDestinationId() == this.identifier) {

            // Hand to the underlying server
            this.passToIntermediary(encapsulation.getPacket()); // Will throw null-pointer if this network device does not have a server attached to it

        } else {

            // Forward to the next switch (automatically advances path progress)
            this.targetIdToOutputPort.get(encapsulation.nextHop()).enqueue(encapsulation);

        }

    }

    /**
     * Returns the paths list originating from this ToR switch.
     *
     * @return  The path list
     */
    public List<List<SourceRoutingPath>> getPathsList(){
    		return destinationToPaths;
    }
    
    /**
     * Receives a TCP packet from the transport layer, which
     * is oblivious to the source routing happening underneath.
     * The TCP packet is then encapsulated to carry information of the
     * route it must take. The sequential hash of the packet is used
     * to determine the path it should be sent on.
     *
     * @param genericPacket     TCP packet instance
     */
    @Override
    public void receiveFromIntermediary(Packet genericPacket) {
        TcpPacket packet = (TcpPacket) genericPacket;
        
	    // Retrieve possible valiant paths to choose from
        List<SourceRoutingPath> possibilities;
        SourceRoutingPath selectedPath;

        if (isWithinExtendedTopology) {

            // Determine source and destination ToR to which the source and destination servers are attached
            int sourceTor = Simulator.getConfiguration().getGraphDetails().getTorIdOfServer(packet.getSourceId());
            int destinationTor = Simulator.getConfiguration().getGraphDetails().getTorIdOfServer(packet.getDestinationId());

            // Create path
            selectedPath = new SourceRoutingPath();

            // If the servers are not on the same ToR
            if (sourceTor != destinationTor) {

                // Retrieve ToR to which it is attached
                SourceRoutingSwitch sourceTorDevice = (SourceRoutingSwitch) this.targetIdToOutputPort.get(sourceTor).getTargetDevice();

                // Retrieve the src-ToR to dst-ToR path possibilities
                possibilities = sourceTorDevice.getPathsList().get(destinationTor);

                // Select a path out of all possibilities...
                SourceRoutingPath orgPath = possibilities.get(packet.getHash(this.identifier) % possibilities.size());

                // ... and add the path to the selected path for its own local copy
                selectedPath.addAll(orgPath);

            } else {

                // If both servers are in the same ToR just create the single up-down path
                selectedPath.add(sourceTor);

            }

            // Now we add both the original server and the destination server to the path
            selectedPath.add(0, packet.getSourceId());
            selectedPath.add(packet.getDestinationId());

        } else {

            // If it is not extended, just use stored paths (all ToRs are servers themselves)
            possibilities = destinationToPaths.get(packet.getDestinationId());
            selectedPath = possibilities.get(packet.getHash(this.identifier) % possibilities.size());

        }
        
        // Create encapsulation to propagate through the network
        SourceRoutingEncapsulation encapsulation = new SourceRoutingEncapsulation(
                packet,
                selectedPath
        );
        
	    // Send to network
        receive(encapsulation);

    }

    /**
     * Add another path possibility to the routing table for the given destination.
     *
     * @param destinationId     Destination identifier
     * @param path              A path instance (first hop must have already been added
     *                          as connection via {@link #addConnection(OutputPort)}, else will throw an illegal
     *                          argument exception. Path must be of non-zero length.
     *                          Path must include this network's identifier as well as the first entry.
     */
    void addPathToDestination(int destinationId, SourceRoutingPath path) {

        // Check for a valid path length
        if (path.size() < 2) {
            throw new IllegalArgumentException("Cannot add a path of zero or one length (must have source and destination included).");
        }

        // Check for not possible next hop identifier
        if (!connectedTo.contains(path.get(1))) {
            throw new IllegalArgumentException("Cannot add path with source hop to a network device to which it is not connected (" + path.get(1) + ")");
        }

        // Source incorrect
        if (path.get(0) != this.getIdentifier()) {
            throw new IllegalArgumentException("First node on path should be this identifier (expected " + this.getIdentifier() + ", received " + path.get(0) + ")");
        }

        // Destination incorrect
        if (path.get(path.size() - 1) != destinationId) {
            throw new IllegalArgumentException("Last node on path should be destination (expected " + destinationId + ", received " + path.get(path.size() - 1) + ")");
        }

        // To itself
        if (this.getIdentifier() == destinationId) {
            throw new IllegalArgumentException("Cannot add a path going to itself (" + this.getIdentifier() + ")");
        }

        // Check for duplicate
        List<SourceRoutingPath> current = this.destinationToPaths.get(destinationId);
        if (current.contains(path)) {
            if (Simulator.getConfiguration().getBooleanPropertyWithDefault("allow_source_routing_skip_duplicate_paths", false)) {
                System.out.println("For (" + this.getIdentifier() + "->" + destinationId + ") skipped duplicate path : " + path);
                return;
            } else if (Simulator.getConfiguration().getBooleanPropertyWithDefault("allow_source_routing_add_duplicate_paths", false)) {
                System.out.println("For (" + this.getIdentifier() + "->" + destinationId + ") added duplicate path : " + path);
            } else {
                throw new IllegalArgumentException("Cannot add a duplicate path (" + path + ")");
            }
        }

        // Add to current ones
        current.add(path);

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SourceRoutingSwitch<id=");
        builder.append(getIdentifier());
        builder.append(", connected=");
        builder.append(connectedTo);
        builder.append(", routing: ");
        for (int i = 0; i < destinationToPaths.size(); i++) {
            builder.append("for ");
            builder.append(i);
            builder.append(" possible paths are ");
            builder.append(destinationToPaths.get(i));
            builder.append("; ");
        }
        builder.append(">");
        return builder.toString();
    }

}
