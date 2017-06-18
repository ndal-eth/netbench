package ch.ethz.systems.netbench.xpt.utility.pairprob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SPPCAllToAllInFraction {

    public static void main(String args[]) throws IOException {

        // Random seed for which fraction to choose
        int seed = 38953;

        // Number of nodes [0, numNodes) is the range of the ToRs
        int numNodes = 200;

        // How many percentage of the nodes is active
        double activePercentageX = 0.3;

        // Amount of servers per server-carrying node
        int serversPerNode = 8;

        // Start of the server indexes, the servers will have indexes [start, start + n * serversPerNode - 1]
        int serverIndexStart = 206;

        // Files
        String serverPairsFile = "temp/server_pair_probabilities.txt";
        String nodePairsFile = "temp/node_pair_probabilities_n" + numNodes + "_active=" + activePercentageX + ".txt";

        /*
         * Decide which nodes are going to participate of the x%.
         */

        // Shuffle nodes
        List<Integer> nodes = new ArrayList<>();
        for (int i = 0; i < numNodes; i++) {
            nodes.add(i);
        }
        Collections.shuffle(nodes, new Random(seed));
        int chosenNodes = (int) Math.floor(numNodes * activePercentageX);

        /*
         * Create node pair probability among the chosen nodes.
         */

        List<Integer> nodePairSrc = new ArrayList<>();
        List<Integer> nodePairDst = new ArrayList<>();
        List<Double> nodePairProb = new ArrayList<>();
        double singleNodePairProb = 1.0 / (chosenNodes * (chosenNodes - 1));
        for (int i = 0; i < chosenNodes; i++) {
            for (int j = 0; j < chosenNodes; j++) {
                if (i != j) {
                    nodePairSrc.add(nodes.get(i));
                    nodePairDst.add(nodes.get(j));
                    nodePairProb.add(singleNodePairProb);
                }
            }
        }

        // Write the node pair probabilities to file
        NodePairProbabilityCreator.writeToFile(
                nodePairsFile,
                "Node pairs for n=" + numNodes + ", participating (" + (activePercentageX * 100) + "%) : " + nodes,
                nodePairSrc,
                nodePairDst,
                nodePairProb
        );

        // Convert the node pair probabilities to server pair probabilities
        SPPCNodePairsFromFile.createServerPairsFromNodePairsFile(serversPerNode, serverIndexStart, nodePairsFile, serverPairsFile);

    }

}
