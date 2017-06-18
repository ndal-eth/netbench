package ch.ethz.systems.netbench.xpt.utility;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ch.ethz.systems.netbench.xpt.utility.SequenceUtility.allInInclusiveRange;
import static ch.ethz.systems.netbench.xpt.utility.SequenceUtility.allUntilExclusive;

public class TrafficPairProbabilitiesCreator {

	public static void generate(List<Integer> participatingNodes, String path) throws IOException {

        // Generate traffic pairs
        List<Pair<Integer, Integer>> trafficPairs = allToAll(participatingNodes.size());

        // Translate traffic pairs
        List<Pair<Integer, Integer>> translatedTrafficPairs = translate(participatingNodes, trafficPairs);

        // Open file output stream
        FileWriter fileStream = new FileWriter(path);
        BufferedWriter probabilitiesFile = new BufferedWriter(fileStream);

        // Write header
        probabilitiesFile.write("# Generated probabilities for participating nodes " + participatingNodes + "\n");
        probabilitiesFile.write("#tor_pair_id,src,dst,pdf_num_bytes\n");

        // Go over pairs and print result
        int id = 0;
        for (Pair<Integer, Integer> pair : translatedTrafficPairs) {
            double pdf = 1.0 / translatedTrafficPairs.size();
            probabilitiesFile.write(id + "," + pair.getLeft() + "," + pair.getRight() + "," + pdf + "\n");
            id++;
        }

        // Close file output stream
        probabilitiesFile.close();

	}
	
    public static void main(String args[]) throws IOException {
        generate(allInInclusiveRange(320, 1343), "temp/created_probabilities.txt");
    }
    
    public static void generate_from_values(Iterable<Pair<Integer,Integer>> values) throws IOException{
		for(Pair<Integer,Integer> p : values){
			int n = p.getLeft();
			int nParticipating = p.getRight();
			System.out.println("Generating results for: " + n);
			generate(allUntilExclusive(nParticipating), "scenarios/all_to_all/all_to_all_n="+Integer.toString(n)+".txt");
		}
    }

    //
    // Traffic functions
    //

    private static List<Pair<Integer, Integer>> allToAll(int n) {
        ArrayList<Pair<Integer, Integer>> ls = new ArrayList<>();
        for (int from = 0; from < n; from++) {
            for (int to = 0; to < n; to++) {
                if (from != to) {
                    ls.add(new ImmutablePair<>(from, to));
                }
            }
        }
        return ls;
    }

    //
    // Translation function
    //

    private static List<Pair<Integer, Integer>> translate(List<Integer> participatingNodes, List<Pair<Integer, Integer>> pairs) {
        List<Pair<Integer, Integer>> translatedPairs = new ArrayList<>();
        for (Pair<Integer, Integer> pair : pairs) {
            translatedPairs.add(new ImmutablePair<>(participatingNodes.get(pair.getLeft()), participatingNodes.get(pair.getRight())));
        }
        return translatedPairs;
    }

}
