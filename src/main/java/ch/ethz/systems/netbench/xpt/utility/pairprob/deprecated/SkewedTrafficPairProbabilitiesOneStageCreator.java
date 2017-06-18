package ch.ethz.systems.netbench.xpt.utility.pairprob.deprecated;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SkewedTrafficPairProbabilitiesOneStageCreator {

    public static void main(String args[]) throws IOException {

        // Number of servers
        int servers = 1024;

        // The higher, the skewer the pair probabilities
        double skewness = 0.15; // 0.95

        // Start of the server indexes, the servers will have indexes [start, start + n * serversPerNode - 1]
        int serverIndexStart = 320;

        // Random shuffle seed for the node probabilities
        int shuffleSeed = 289895;

        // Out file names
        String outNodePairsFileName = "temp/server_pair_probabilities_one_stage.csv";

        /* ***************************************
         * 1) Calculate node pair probabilities
         */

        // Calculate skewed fabricated mass for each node
        List<Double> mass = new ArrayList<>();
        double defaultMass = 1 / (2.0 * servers);
        double totalMass = 0.0;
        for (int i = 0; i < servers; i++) {
            double val = skewedExponentialFunction(skewness, i) + defaultMass;
            mass.add(val);
            totalMass += val;
        }

        // Calculate probability of a node being involved in a pair
        List<Double> nodeProb = new ArrayList<>();
        for (int i = 0; i < servers; i++) {
            nodeProb.add(mass.get(i) / totalMass);
        }
        double top1percTotalProb = 0.0;
        double top5percTotalProb = 0.0;
        double top10percTotalProb = 0.0;
        for (int i = 0; i < servers / 10; i++) {
            if (i < servers / 100) {
                top1percTotalProb += nodeProb.get(i);
            }
            if (i < servers / 20) {
                top5percTotalProb += nodeProb.get(i);
            }
            if (i < servers / 10) {
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
        for (int i = 0; i < servers; i++) {
            for (int j = 0; j < servers; j++) {
                double prob = i == j ? 0 : nodeProb.get(i) * nodeProb.get(j);
                nodePairSrc.add(serverIndexStart + i);
                nodePairDst.add(serverIndexStart + j);
                nodePairProb.add(prob);
                totalProbLeft += prob;
            }
        }

        // Normalize again for the missing diagonal
        double max = 0.0;
        for (int i = 0; i < servers * servers; i++) {
            double val = nodePairProb.get(i) / totalProbLeft;
            max = Math.max(max, val);
            nodePairProb.set(i, val);
        }
        System.out.println("Maximum node pair probability: " + max);

        // Write away node probabilities
        BufferedWriter nodeOutWriter = new BufferedWriter(new FileWriter(outNodePairsFileName));
        nodeOutWriter.write("# Skewed traffic node pairs; servers=" + servers +
                ", skewness=" + skewness +
                ", serverStartIdx=" + serverIndexStart +
                ", shuffleSeed=" + shuffleSeed + "\n"
        );
        nodeOutWriter.write("#node_pair_id,src,dst,pdf_num_bytes\n");
        for (int i = 0; i < servers * servers; i++) {
            if (!nodePairSrc.get(i).equals(nodePairDst.get(i))) {
                nodeOutWriter.write(i + "," + nodePairSrc.get(i) + "," + nodePairDst.get(i) + "," + nodePairProb.get(i) + "\n");
            }
        }
        nodeOutWriter.close();

    }

    public static double skewedExponentialFunction(double skewness, int x) {
        return 0.05 * Math.exp(-1 * skewness * x);
    }

}
