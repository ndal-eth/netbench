package ch.ethz.systems.netbench.core.run.infrastructure;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.GraphDetails;
import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.network.OutputPort;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Vertex;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Initializes the entire infrastructure in the most
 * generic way possible using the given generators.
 *
 * Any differentiation between applications, transport layers,
 * output ports, links or nodes are decided by
 * their respective generators.
 */
public class BaseInitializer {

    // Mappings
    private final Map<Integer, NetworkDevice> idToNetworkDevice;
    private final Map<Integer, TransportLayer> idToTransportLayer;

    // Generators
    private final OutputPortGenerator outputPortGenerator;
    private final NetworkDeviceGenerator networkDeviceGenerator;
    private final LinkGenerator linkGenerator;
    private final TransportLayerGenerator transportLayerGenerator;

    // Validation variables
    private int runningNodeId;
    private boolean infrastructureAlreadyCreated;
    private List<Pair<Integer, Integer>> linkPairs;

    public BaseInitializer(
            OutputPortGenerator outputPortGenerator,
            NetworkDeviceGenerator networkDeviceGenerator,
            LinkGenerator linkGenerator,
            TransportLayerGenerator transportLayerGenerator
    ) {
        this.idToNetworkDevice = new HashMap<>();
        this.idToTransportLayer = new HashMap<>();
        this.outputPortGenerator = outputPortGenerator;
        this.networkDeviceGenerator = networkDeviceGenerator;
        this.linkGenerator = linkGenerator;
        this.transportLayerGenerator = transportLayerGenerator;
        this.runningNodeId = 0;
        this.infrastructureAlreadyCreated = false;
        this.linkPairs = new ArrayList<>();
    }

    /**
     * Read the base network (servers, network devices, links) from the topology file.
     */
    public void createInfrastructure() {

        // The infrastructure can only be created once
        if (infrastructureAlreadyCreated) {
            throw new RuntimeException("Impossible to create infrastructure twice.");
        }

        // Fetch from configuration graph and its details
        Graph graph = Simulator.getConfiguration().getGraph();
        GraphDetails details = Simulator.getConfiguration().getGraphDetails();

        // Create nodes
        for (int i = 0; i < details.getNumNodes(); i++) {
            createNode(i, details.getServerNodeIds().contains(i));
        }

        // Create edges
        for (Vertex v : graph.getVertexList()) {
            for (Vertex w : graph.getAdjacentVertices(v)) {
                createEdge(v.getId(), + w.getId());
            }
        }

        // Check the links for bi-directionality
        for (int i = 0; i < linkPairs.size(); i++) {

            // Attempt to find the reverse
            boolean found = false;
            for (int j = 0; j < linkPairs.size(); j++) {
                if (i != j && linkPairs.get(j).equals(new ImmutablePair<>(linkPairs.get(i).getRight(), linkPairs.get(i).getLeft()))) {
                    found = true;
                    break;
                }
            }

            // If reverse not found, it is not bi-directional
            if (!found) {
                throw new IllegalArgumentException(
                        "Link was added which is not bi-directional: " +
                        linkPairs.get(i).getLeft() + " -> " + linkPairs.get(i).getRight()
                );
            }

        }

    }

    /**
     * Create the implementation of a node in the network.
     *
     * @param id        Node identifier
     * @param isServer  True iff it is a server (a.k.a. will have a transport layer)
     */
    private void createNode(int id, boolean isServer) {

        // Make sure that the node identifiers are in sequence
        if (id != runningNodeId) {
            throw new IllegalArgumentException(
                    "A node identifier has been skipped. " +
                    "Expected " + runningNodeId + " but next was " + id + ". Please check input topology file.");
        }
        runningNodeId++;

        // Create corresponding network device
        NetworkDevice networkDevice;
        if (isServer) {

            // Create server
            TransportLayer transportLayer = transportLayerGenerator.generate(id);

            // Create network device
            networkDevice = networkDeviceGenerator.generate(id, transportLayer);
            idToTransportLayer.put(id, transportLayer);

            // Link transport layer to network device
            transportLayer.setNetworkDevice(networkDevice);

        } else {
            networkDevice = networkDeviceGenerator.generate(id);
        }
        
        // Add to mappings
        idToNetworkDevice.put(id, networkDevice);

    }

    /**
     * Create the implementation of a directed edge in the network.
     *
     * @param startVertexId     Origin vertex identifier
     * @param endVertexId       Destination vertex identifier
     */
    private void createEdge(int startVertexId, int endVertexId) {

        // Select network devices
        NetworkDevice devA = idToNetworkDevice.get(startVertexId);
        NetworkDevice devB = idToNetworkDevice.get(endVertexId);

        // Add connection
        OutputPort portAtoB = outputPortGenerator.generate(
                devA,
                devB,
                linkGenerator.generate(devA, devB)
        );
        devA.addConnection(portAtoB);

        // Duplicate link
        if (linkPairs.contains(new ImmutablePair<>(startVertexId, endVertexId))) {
            throw new IllegalArgumentException(
                    "Duplicate link (" + startVertexId + " -> + " + endVertexId +
                    ") defined - Please check input topology file."
            );
        } else {
            linkPairs.add(new ImmutablePair<>(startVertexId, endVertexId));
        }

    }

    /**
     * Retrieve identifier to network device mapping.
     *
     * @return  Mapping of node identifier to its network device
     */
    public Map<Integer, NetworkDevice> getIdToNetworkDevice() {
        return idToNetworkDevice;
    }

    /**
     * Retrieve identifier to transport layer mapping.
     *
     * @return  Mapping of node identifier to its transport layer
     */
    public Map<Integer, TransportLayer> getIdToTransportLayer() {
        return idToTransportLayer;
    }

}
