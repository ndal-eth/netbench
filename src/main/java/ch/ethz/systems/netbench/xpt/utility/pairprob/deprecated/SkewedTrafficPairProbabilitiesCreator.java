package ch.ethz.systems.netbench.xpt.utility.pairprob.deprecated;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SkewedTrafficPairProbabilitiesCreator {

    public static void main(String args[]) throws IOException {

        // Number of server-carrying nodes
        int n = 192;

        // The higher, the skewer the pair probabilities
        double skewness = 0.321; // 0.95

        // Amount of servers per server-carrying node
        int serversPerNode = 6;

        // Start of the server indexes, the servers will have indexes [start, start + n * serversPerNode - 1]
        int serverIndexStart = 192;

        // Random shuffle seed for the node probabilities
        int shuffleSeed = 289895;

        // Out file names
        String outNodePairsFileName = "temp/node_pair_probabilities.csv";
        String outServerPairsFileName = "temp/server_pair_probabilities.csv";

        /* ***************************************
         * 1) Calculate node pair probabilities
         */

        // Calculate skewed fabricated mass for each node
        List<Double> mass = new ArrayList<>();
        double defaultMass = 1 / (2.0 * n);
        double totalMass = 0.0;
        for (int i = 0; i < n; i++) {
            double val = skewedExponentialFunction(skewness, i) + defaultMass;
            mass.add(val);
            totalMass += val;
        }

        // Calculate probability of a node being involved in a pair
        List<Double> nodeProb = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            nodeProb.add(mass.get(i) / totalMass);
        }
        double top1percTotalProb = 0.0;
        double top5percTotalProb = 0.0;
        double top10percTotalProb = 0.0;
        for (int i = 0; i < n / 10; i++) {
            if (i < n / 100) {
                top1percTotalProb += nodeProb.get(i);
            }
            if (i < n / 20) {
                top5percTotalProb += nodeProb.get(i);
            }
            if (i < n / 10) {
                top10percTotalProb += nodeProb.get(i);
            }
        }
        System.out.println("The top 1% nodes hold " + (100 * top1percTotalProb) + "% of all node probability.");
        System.out.println("The top 5% nodes hold " + (100 * top5percTotalProb) + "% of all node probability.");
        System.out.println("The top 10% nodes hold " + (100 * top10percTotalProb) + "% of all node probability.");
        System.out.println("Maximum node probability: " + nodeProb.get(0));

        // Shuffle probability list such that there is no guarantee which node
        // will become the most probable (as mass is drawn from exponential distribution left-to-right)
        Collections.shuffle(nodeProb, new Random(shuffleSeed));

        // Calculate all node pair probabilities
        List<Integer> nodePairSrc = new ArrayList<>();
        List<Integer> nodePairDst = new ArrayList<>();
        List<Double> nodePairProb = new ArrayList<>();
        double totalProbLeft = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double prob = i == j ? 0 : nodeProb.get(i) * nodeProb.get(j);
                nodePairSrc.add(i);
                nodePairDst.add(j);
                nodePairProb.add(prob);
                totalProbLeft += prob;
            }
        }

        // Normalize again for the missing diagonal
        double max = 0.0;
        for (int i = 0; i < n * n; i++) {
            double val = nodePairProb.get(i) / totalProbLeft;
            max = Math.max(max, val);
            nodePairProb.set(i, val);
        }
        System.out.println("Maximum node pair probability: " + max);

        // Write away node probabilities
        BufferedWriter nodeOutWriter = new BufferedWriter(new FileWriter(outNodePairsFileName));
        nodeOutWriter.write("# Skewed traffic node pairs; n=" + n +
                ", skewness=" + skewness +
                ", serverStartIdx=" + serverIndexStart +
                ", serversPerNode=" + serversPerNode +
                ", shuffleSeed=" + shuffleSeed + "\n"
        );
        nodeOutWriter.write("#node_pair_id,src,dst,pdf_num_bytes\n");
        for (int i = 0; i < n * n; i++) {
            if (!nodePairSrc.get(i).equals(nodePairDst.get(i))) {
                nodeOutWriter.write(i + "," + nodePairSrc.get(i) + "," + nodePairDst.get(i) + "," + nodePairProb.get(i) + "\n");
            }
        }
        nodeOutWriter.close();

        /* ***************************************
         * 2) Calculate node pair probabilities
         */

        // Open file output stream
        BufferedWriter outWriter = new BufferedWriter(new FileWriter(outServerPairsFileName));

        // Write header
        outWriter.write("# Skewed traffic server pairs; n=" + n +
                ", skewness=" + skewness +
                ", serverStartIdx=" + serverIndexStart +
                ", serversPerNode=" + serversPerNode +
                ", shuffleSeed=" + shuffleSeed + "\n"
        );
        outWriter.write("#server_pair_id,src,dst,pdf_num_bytes\n");

        // Read in others
        int srvPairId = 0;
        double totalProb = 0;
        for (int i = 0; i < n * n; i++) {

            if (!nodePairSrc.get(i).equals(nodePairDst.get(i))) {

                // Write away server probability nodes
                int srvSrcStartId = serverIndexStart + nodePairSrc.get(i) * serversPerNode;
                int srvDstStartId = serverIndexStart + nodePairDst.get(i) * serversPerNode;
                double serverProb = nodePairProb.get(i) / (double) (serversPerNode * serversPerNode);
                for (int z = srvSrcStartId; z < srvSrcStartId + serversPerNode; z++) {
                    for (int j = srvDstStartId; j < srvDstStartId + serversPerNode; j++) {
                        outWriter.write(srvPairId + "," + z + "," + j + "," + serverProb + "\n");
                        srvPairId++;
                        totalProb += serverProb;
                    }
                }

            }

        }

        System.out.println("Total server pair probability: " + totalProb);

        // Close file streams
        outWriter.close();

    }

    public static double skewedExponentialFunction(double skewness, int x) {
        return 2 * Math.exp(-1 * skewness * x);
    }

}
