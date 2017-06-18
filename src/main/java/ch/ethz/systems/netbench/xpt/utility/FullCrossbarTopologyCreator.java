package ch.ethz.systems.netbench.xpt.utility;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Main utility to create a full crossbar (fully connected)
 * topology scenario file for any given number of nodes n.
 */
public class FullCrossbarTopologyCreator {

    public static void main(String[] args) throws IOException {

        int n = 198;

        // Open file output stream
        FileWriter fileStream = new FileWriter("temp/topology_crossbar_n" + n + ".txt");
        BufferedWriter topologyFile = new BufferedWriter(fileStream);

        // Print header
        topologyFile.write("# Full crossbar for n=" + n + "\n\nSTART PARAMS\nN=" + n + "\nEND PARAMS\n\nSTART NODES\n");

        // Print that all nodes have a transport layer
        for (int i = 0; i < n; i++) {
            topologyFile.write(i + " 1\n");
        }

        topologyFile.write("END NODES\n\nSTART LINKS\n");

        // Print all links
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    topologyFile.write(i + " " + j + "\n");
                }
            }
        }

        topologyFile.write("END LINKS");

        topologyFile.close();

    }

}
