package ch.ethz.systems.netbench.xpt.utility;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static ch.ethz.systems.netbench.xpt.utility.SequenceUtility.allInInclusiveRange;

/**
 * Main utility tool to generate the link list for uniformly adding servers
 * to an already existing topology of nodes.
 */
public class UniformServerLinksToNodesCreator {

    public static void main(String[] args) throws IOException {

        // E.g. a fat-tree of k=16 has 320 nodes, 0-127 indexes are the bottom layer,
        // and each of the bottom layer nodes has k/2=8 servers
        //
        // So, 320 until 320 + 128 * 8 - 1 = 1343 will be the server indexes
        generateServerLinks(allInInclusiveRange(0, 127), 320, 8, "temp/additional_server_links.txt");

    }

    /**
     * Writes additional server links to a file.
     *
     * @param nodesWithServers  List of node identifiers that carry the servers
     * @param serverIndexStart  Index start (inclusive) of the servers
     * @param serversPerNode    Amount of nodes to be added to each node
     * @param fileName          File name where to write the server links to
     */
    private static void generateServerLinks(List<Integer> nodesWithServers, int serverIndexStart, int serversPerNode, String fileName) throws IOException {

        // Open file output stream
        FileWriter fileStream = new FileWriter(fileName);
        BufferedWriter writer = new BufferedWriter(fileStream);

        // Create additional bi-directional links between server and assigned node
        int i = 0;
        for (Integer nodeId : nodesWithServers) {
            for (int j = 0; j < serversPerNode; j++) {
                int serverId = serverIndexStart + i * serversPerNode + j;
                writer.write(serverId + " " + nodeId + "\n");
                writer.write(nodeId + " " + serverId + "\n");
            }
            i++;
        }

        // Close file output stream
        writer.close();

        // End message
        System.out.println("Uniform server links are generated; output in file " + fileName);

    }

}