package ch.ethz.systems.netbench.xpt.utility.pairprob;


import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SPPCDegreeOfSkew {

    public static void main(String args[]) throws IOException {

        // Random seed for which fraction to choose
        int seed = 38953;

        // Number of nodes [0, numNodes) is the range of the ToRs
        int numNodes = 200;

        // The higher, the skewer the pair probabilities
        double skewness = 0.321;

        // Amount of servers per server-carrying node
        int serversPerNode = 8;

        // Start of the server indexes, the servers will have indexes [start, start + n * serversPerNode - 1]
        int serverIndexStart = 192;

        // Files
        String nodePairsFile = "temp/node_pair_probabilities_n" + numNodes + "_skewness=" + skewness + ".txt";
        String serverPairsFile = "temp/server_pair_probabilities.txt";

        // Shuffle nodes
        List<Pair<Integer, Integer>> nodePairs = new ArrayList<>();
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                nodePairs.add(new ImmutablePair<>(i, j));
            }
        }
        Collections.shuffle(nodePairs, new Random(seed));

        // Determine node pair probabilities
        List<Double> nodePairProb = new ArrayList<>();
        for (int i = 0; i < nodePairs.size(); i++) {
            nodePairProb.add(0.000); // TODO
        }

        // Convert to handy format
        List<Integer> nodePairSrc = new ArrayList<>();
        List<Integer> nodePairDst = new ArrayList<>();
        for (Pair<Integer, Integer> nodePair : nodePairs) {
            nodePairSrc.add(nodePair.getLeft());
            nodePairDst.add(nodePair.getRight());
        }

        // Write the node pair probabilities to file
        NodePairProbabilityCreator.writeToFile(
                nodePairsFile,
                "Node pairs for n=" + numNodes + ", skewness (" + skewness + ")",
                nodePairSrc,
                nodePairDst,
                nodePairProb
        );

        // Convert node pair probabilities to uniform server pair probabilities
        SPPCNodePairsFromFile.createServerPairsFromNodePairsFile(serversPerNode, serverIndexStart, nodePairsFile, serverPairsFile);

    }

}
