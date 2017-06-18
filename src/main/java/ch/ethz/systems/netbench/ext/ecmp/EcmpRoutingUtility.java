package ch.ethz.systems.netbench.ext.ecmp;

import ch.ethz.systems.netbench.core.network.NetworkDevice;
import ch.ethz.systems.netbench.core.Simulator;
import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Vertex;

import java.util.List;
import java.util.Map;

public class EcmpRoutingUtility {

    private static final int INFINITY = 999999999;

    private EcmpRoutingUtility() {
        // Cannot be instantiated
    }

    /**
     * Calculate all the shortest paths and store them internally.
     * Uses the modified Floyd-Warshall algorithm.
     */
    private static int[][] calculateShortestPaths(Graph graph) {

        System.out.print("Calculating shortest path lengths...");

        int numNodes = graph.getVertexList().size();
        int[][] shortestPathLen = new int[numNodes][numNodes];

        // Initial scan to find easy shortest paths
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if (i == j) {
                    shortestPathLen[i][j] = 0;              // To itself
                } else if (graph.getAdjacentVertices(graph.getVertex(i)).contains(graph.getVertex(j))) {
                    shortestPathLen[i][j] = 1;              // To direct neighbor
                } else {
                    shortestPathLen[i][j] = INFINITY;       // To someone not directly connected
                }
            }
        }

        // Floyd-Warshall algorithm
        for (int k = 0; k < numNodes; k++) {
            for (int i = 0; i < numNodes; i++) {
                for (int j = 0; j < numNodes; j++) {
                    if (shortestPathLen[i][j] > shortestPathLen[i][k] + shortestPathLen[k][j]) {
                        shortestPathLen[i][j] = shortestPathLen[i][k] + shortestPathLen[k][j];
                    }
                }
            }
        }

        System.out.println(" done.");

        return shortestPathLen;

    }

    /**
     * Initializes the multi-forwarding ECMP routing tables in the network devices.
     * The network devices must be ECMP switches, and should have been generated
     * corresponding to the topology graph defined in the run configuration.
     *
     * @param idToNetworkDevice     Mapping of network device identifier to network device
     */
    public static void populateShortestPathRoutingTables(Map<Integer, NetworkDevice> idToNetworkDevice, boolean isEcmp) {

        // Create graph and prepare shortest path algorithm
        Graph graph = Simulator.getConfiguration().getGraph();
        int numNodes = Simulator.getConfiguration().getGraphDetails().getNumNodes();

        // Calculate shortest path length
        int[][] shortestPathLen = EcmpRoutingUtility.calculateShortestPaths(graph);

        System.out.print("Populating ECMP forward routing tables...");

        // Go over every network device pair and set the forwarder switch routing table
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if (i != j) {

                    // For every outgoing edge (i, j) check if it is on a shortest path to j
                    List<Vertex> adjacent = graph.getAdjacentVertices(graph.getVertex(i));
                    for (Vertex v : adjacent) {

                        // ECMP stores all the possible hops
                        if (isEcmp) {

                            if (shortestPathLen[i][j] == shortestPathLen[v.getId()][j] + 1) {
                                ((EcmpSwitchRoutingInterface) idToNetworkDevice.get(i)).addDestinationToNextSwitch(j, v.getId());
                            }

                        // ... whereas single-forward routing only stores a single hop entry
                        } else {
                            if (shortestPathLen[i][j] == shortestPathLen[v.getId()][j] + 1) {
                                ((ForwarderSwitch) idToNetworkDevice.get(i)).setDestinationToNextSwitch(j, v.getId());
                                break; // We only need a single possibility
                            }
                        }

                    }
                }

            }

            // Log progress...
            if (numNodes > 10 && (i + 1) % ((numNodes / 10)) == 0) {
                System.out.print(" " + (((double) i + 1) / (numNodes) * 100) + "%...");
            }

        }

        System.out.println(" done.");

    }

}
