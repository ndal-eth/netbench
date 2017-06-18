package ch.ethz.systems.netbench.xpt.utility.pairprob;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/*
 * Server pair probability creator using node pairs from a file.
 */
public class SPPCNodePairsFromFile {

    public static void main(String args[]) throws IOException {

        // Amount of servers per server-carrying node
        int serversPerNode = 8;

        // Start of the server indexes, the servers will have indexes [start, start + n * serversPerNode - 1]
        int serverIndexStart = 206;

        // Input of node pairs file
        String inNodePairsFileName = "scenarios/projector/projector_tors_128_probabilities.xlarge.txt";

        // Output of servers pairs file
        String outServerPairsFileName = "temp/server_pair_probabilities.csv";

        // Call
        createServerPairsFromNodePairsFile(serversPerNode, serverIndexStart, inNodePairsFileName, outServerPairsFileName);

    }

    /**
     * Convert a node pairs file into a servers pairs file.
     *
     * WARNING: operates under the assumption that nodes are assigned servers
     * sequentially, as in with n=4, server_start=4, servers_per_node=4
     *
     * Node 0 has servers 4, 5, 6, 7
     * Node 1 has servers 8, 9, 10, 11
     * Node 2 has servers 12, 13, 14, 15
     * Node 3 has servers 16, 17, 18, 19
     *
     * @param serversPerNode            Servers per node
     * @param serverIndexStart          Start of the server indexes (not occupied by node pair indices)
     * @param inNodePairsFileName       Node pairs input file name
     * @param outServerPairsFileName    Server pairs output file name
     *
     * @throws IOException
     */
    public static void createServerPairsFromNodePairsFile(
            int serversPerNode,
            int serverIndexStart,
            String inNodePairsFileName,
            String outServerPairsFileName
    )
    throws IOException {

        /* ***************************************
         * 1) Read in node pair probabilities
         */

        // Storage for node pairs
        List<Integer> nodePairSrc = new ArrayList<>();
        List<Integer> nodePairDst = new ArrayList<>();
        List<Double> nodePairProb = new ArrayList<>();

        // Open file stream
        FileReader input = new FileReader(inNodePairsFileName);
        BufferedReader br = new BufferedReader(input);

        // Go over all lines
        String line;

        // Read in others
        while ((line = br.readLine()) != null) {

            // Remove trailing whitespace
            line = line.trim();

            // Skip empty lines
            if (line.equals("") || line.startsWith("#")) {
                continue;
            }

            String[] spl = line.split(",");

            if (spl.length != 4) {
                throw new RuntimeException("Communication probability line must have [tor_pair_id, src, dst, pdf_num_bytes] but was:\n" + line);
            }

            // Convert to correct format
            // int torPairId = Integer.valueOf(spl[0]);
            int src = Integer.valueOf(spl[1]);
            int dst = Integer.valueOf(spl[2]);
            double pdfNumBytes = Double.valueOf(spl[3]);
            nodePairSrc.add(src);
            nodePairDst.add(dst);
            nodePairProb.add(pdfNumBytes);


        }

        // Close file stream
        br.close();

        /* ***************************************
         * 2) Calculate server pair probabilities
         */

        // Open file output stream
        BufferedWriter outWriter = new BufferedWriter(new FileWriter(outServerPairsFileName));
        outWriter.write("#server_pair_id,src,dst,pdf_num_bytes\n");

        // Go over every node pair
        int srvPairId = 0;
        double totalProb = 0;
        for (int i = 0; i < nodePairSrc.size(); i++) {

            if (nodePairSrc.get(i).equals(nodePairDst.get(i))) {
                throw new RuntimeException("Illegal node pair, cannot go to itself: " +
                        nodePairSrc.get(i) + " -> " +
                        nodePairDst.get(i) + " (" +
                        nodePairProb.get(i) + ")"
                );
            }

            // Write away server probability nodes
            int srvSrcStartId = serverIndexStart + nodePairSrc.get(i) * serversPerNode;
            int srvDstStartId = serverIndexStart + nodePairDst.get(i) * serversPerNode;
            double serverProb = nodePairProb.get(i) / (double) (serversPerNode * serversPerNode);

            // Go over every server combination
            for (int z = srvSrcStartId; z < srvSrcStartId + serversPerNode; z++) {
                for (int j = srvDstStartId; j < srvDstStartId + serversPerNode; j++) {
                    outWriter.write(srvPairId + "," + z + "," + j + "," + serverProb + "\n");
                    srvPairId++;
                    totalProb += serverProb;
                }
            }

        }

        System.out.println("Total server pair probability: " + totalProb);

        // Close file streams
        outWriter.close();

    }

}
