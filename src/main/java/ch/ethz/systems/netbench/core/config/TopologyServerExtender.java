package ch.ethz.systems.netbench.core.config;

import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Vertex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class TopologyServerExtender {

    private String topologyFileNameIn;
    private String topologyFileNameOut;

    public TopologyServerExtender(String topologyFileNameIn, String topologyFileNameOut) {
        this.topologyFileNameIn = topologyFileNameIn;
        this.topologyFileNameOut = topologyFileNameOut;
    }

    /**
     * Extends an existing topology file with servers.
     * It does so by adding s additional nodes with transport layer to
     * every node marked as ToR in the input topology file.
     *
     * @param serversPerTransportLayerNode      Desired number of serves to be added to each transport layer node
     */
    public void extendRegular(int serversPerTransportLayerNode) {

        // Read in original graph to extend
        Pair<Graph, GraphDetails> original = GraphReader.read(topologyFileNameIn);
        Graph graph = original.getLeft();
        GraphDetails details = original.getRight();

        // Check that the graph can be extended
        if (!details.getTorNodeIds().equals(details.getServerNodeIds())) {
            throw new IllegalArgumentException(
                    "Only a topology of which all its ToRs are marked as servers, " +
                            "and nothing else, is possible to be extended."
            );
        }

        try {

            System.out.println(
                    "Extending topology file \"" + topologyFileNameIn + "\" by adding " +
                            serversPerTransportLayerNode + " servers to each ToR."
            );


            System.out.print("Writing to output topology file...");

            // Open output file stream
            BufferedWriter outputWriter = new BufferedWriter(new FileWriter(topologyFileNameOut));

            // Header
            outputWriter.write("# Extension with " + serversPerTransportLayerNode + " servers/TL node of topology file \"" + topologyFileNameIn + "\"\n\n");

            // Pre-calculate what the new N parameter will be
            int originalN = details.getNumNodes();
            int additionalNodes = serversPerTransportLayerNode * details.getServerNodeIds().size();
            int newN = originalN + additionalNodes;

            // Write details
            outputWriter.write("# Extended details\n");
            outputWriter.write("|V|=" + newN + "\n");
            outputWriter.write("|E|=" + (details.getNumEdges() + additionalNodes * 2) + "\n");
            outputWriter.write("AutoExtended=true\n");
            outputWriter.write("Servers=incl_range(" + originalN + ", " + (newN - 1) + ")\n");
            outputWriter.write("Switches=set(" + StringUtils.join(details.getSwitchNodeIds(), ",") + ")\n");
            outputWriter.write("ToRs=set(" + StringUtils.join(details.getTorNodeIds(), ",") + ")\n\n");

            // Links section
            outputWriter.write("# Original " + details.getNumEdges() + " links:\n");
            for (Vertex v : graph.getVertexList()) {
                for (Vertex w : graph.getAdjacentVertices(v)) {
                    outputWriter.write(v.getId() + " " + w.getId() + "\n");
                }
            }

            outputWriter.write("\n# Extended " + (additionalNodes * 2) + " links:\n");
            int addedSoFar = 0;
            for (Integer nodeId : details.getTorNodeIds()) {
                for (int s = 0; s < serversPerTransportLayerNode; s++) {
                    outputWriter.write((originalN + addedSoFar) + " " + nodeId + "\n");
                    outputWriter.write(nodeId + " " + (originalN + addedSoFar) + "\n");
                    addedSoFar++;
                }
            }

            // Close output file stream
            outputWriter.close();

            System.out.println(" done.");

        } catch (IOException e) {
            throw new RuntimeException("TopologyServerExtender: failed to read/write a topology file: " + e.getMessage());
        }

    }

}
