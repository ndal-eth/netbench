package ch.ethz.systems.netbench.xpt.utility.pairprob;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

class NodePairProbabilityCreator {

    static void writeToFile(
        String file,
        String headerInfo,
        List<Integer> nodePairSrc,
        List<Integer> nodePairDst,
        List<Double> nodePairProb
    ) throws IOException {

        // Check sizes
        if (nodePairSrc.size() != nodePairDst.size() || nodePairDst.size() != nodePairProb.size()) {
            throw new RuntimeException("Invalid node pair list sizes.");
        }

        int numNodePairs = nodePairSrc.size();

        // Open file
        BufferedWriter nodeOutWriter = new BufferedWriter(new FileWriter(file));

        // Write header
        nodeOutWriter.write("# " + headerInfo + "\n");
        nodeOutWriter.write("#node_pair_id,src,dst,pdf_num_bytes\n");

        // Write all pairs
        for (int i = 0; i < numNodePairs; i++) {

            // Pair condition
            if (nodePairSrc.get(i).equals(nodePairDst.get(i)) && nodePairProb.get(i) != 0.0) {
                throw new RuntimeException("Cannot have a node pair with probability of communicating to itself: nodeId=" + nodePairSrc.get(i));
            }

            // No need to add 0 probability cases
            if (!nodePairSrc.get(i).equals(nodePairDst.get(i))) {
                nodeOutWriter.write(i + "," + nodePairSrc.get(i) + "," + nodePairDst.get(i) + "," + nodePairProb.get(i) + "\n");
            }

        }

        // Close file
        nodeOutWriter.close();

    }

}
