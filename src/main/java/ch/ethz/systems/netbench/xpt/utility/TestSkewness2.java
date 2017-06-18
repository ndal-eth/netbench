package ch.ethz.systems.netbench.xpt.utility;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.GraphDetails;
import ch.ethz.systems.netbench.core.config.GraphReader;
import ch.ethz.systems.netbench.core.config.TopologyServerExtender;
import ch.ethz.systems.netbench.ext.poissontraffic.ParetoDistribution;
import edu.asu.emit.algorithm.graph.Graph;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TestSkewness2 {

    public static void main(String args[]) {

            TopologyServerExtender ext = new TopologyServerExtender("example/topologies/diamond/diamond_n5.topology", "temp/topo.txt");
            //TopologyServerExtender ext = new TopologyServerExtender("private/data/topologies/xpander_n216_d11.topology", "temp/topo.txt");

            ext.extendRegular(7);
            Pair<Graph, GraphDetails> result = GraphReader.read("temp/topo.txt");

            List<Integer> tors = new ArrayList<>(result.getRight().getTorNodeIds());
            //new ArrayList<>(Simulator.getConfiguration().getGraphDetails().getTorNodeIds());


            // Retrieve necessary parameters from the extension
            int numTors = tors.size();
            //int serversPerNodeToExtendWith = Simulator.getConfiguration().getIntegerPropertyOrFail("scenario_topology_extend_servers_per_tl_node");
            boolean fractionIsOrdered = true; // Simulator.getConfiguration().getBooleanPropertyOrFail("traffic_probabilities_active_fraction_is_ordered");
            double activeFractionA = 0.5; //Simulator.getConfiguration().getDoublePropertyOrFail("traffic_probabilities_fraction_A");
            double probabilityMassA = 0.8; //Simulator.getConfiguration().getDoublePropertyOrFail("traffic_probabilities_mass_A");


            // Shuffle nodes
            /*List<Integer> tors = new ArrayList<>();
            for (int i = 0; i < numTors; i++) {
                tors.add(i);
            }*/
            if (!fractionIsOrdered) {
                Collections.shuffle(tors, Simulator.selectIndependentRandom("all_to_all_fraction_shuffle"));
            }
            int numChosenTorsA = (int) Math.floor(numTors * activeFractionA);
            int numChosenTorsB = numTors - numChosenTorsA;

            double probabilityPerA = probabilityMassA / (double) numChosenTorsA;
            double probabilityPerB = (1.0 - probabilityMassA) / (double) numChosenTorsB;

            // Calculate how much probability is wasted on the diagonal
            double wastedProbability = 0.0;
            wastedProbability += numChosenTorsA * probabilityPerA * probabilityPerA;
            wastedProbability += numChosenTorsB * probabilityPerB * probabilityPerB;

            // Go over every ToR pair
        double  s= 0;
            for (int i = 0; i < tors.size(); i++) {
                double torProbI = i < numChosenTorsA ? probabilityPerA : probabilityPerB;
                for (int j = 0; j < tors.size(); j++) {
                    double torProbJ = j < numChosenTorsA ? probabilityPerA : probabilityPerB;
                    if (i != j) {

                        // ToR-pair probability with diagonal waste normalized out
                        double torPairProb = torProbI * torProbJ / (1 - wastedProbability);
                        System.out.println(torPairProb);
                        // Servers
                        List<Integer> srcServers = new ArrayList<>(result.getRight().getServersOfTor(tors.get(i)));
                        List<Integer> dstServers = new ArrayList<>(result.getRight().getServersOfTor(tors.get(j)));

                        double serverProb = torPairProb / (srcServers.size() * dstServers.size());
                        for (int src : srcServers) {
                            for (int dst : dstServers) {
                                s += serverProb;
                            }
                        }

                    }

                }
            }
        System.out.println(s);



    }

}
