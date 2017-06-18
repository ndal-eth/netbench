package ch.ethz.systems.netbench.xpt.utility;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.GraphDetails;
import ch.ethz.systems.netbench.core.config.GraphReader;
import ch.ethz.systems.netbench.core.config.TopologyServerExtender;
import ch.ethz.systems.netbench.ext.poissontraffic.ParetoDistribution;
import edu.asu.emit.algorithm.graph.Graph;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class TestSkewness {

    public static void main(String args[]) {

        System.out.println("Uniform value: " + 1.0/216.0);
        System.out.print("Generating pareto-skewed pair probabilities between all nodes with a transport layer...");
        double[] shapes = new double[]{0.01, 0.1, 0.15, 0.2, 0.5, 0.8, 1, 1.01, 1.03, 1.05, 1.5, 3, 5, 10, 20, 40, 100};

        for (double shape : shapes) {
            //TopologyServerExtender ext = new TopologyServerExtender("example/topologies/diamond/diamond_n5.topology", "temp/topo.txt");
            TopologyServerExtender ext = new TopologyServerExtender("private/data/topologies/xpander_n216_d11.topology", "temp/topo.txt");

            ext.extendRegular(7);
            Pair<Graph, GraphDetails> result = GraphReader.read("temp/topo.txt");

            List<Integer> tors = new ArrayList<>(result.getRight().getTorNodeIds());
            //new ArrayList<>(Simulator.getConfiguration().getGraphDetails().getTorNodeIds());


            // Get the random generator for this part
            Random gen = new Random(355560); // Simulator.selectIndependentRandom("skew_pareto_distribution");
            ParetoDistribution pareto = new ParetoDistribution(
                    shape,
                    10, // Scale does not matter because of normalization
                    gen
            );
            Collections.shuffle(tors, gen);

            // For every pair, draw their "probability mass" from the
            // Pareto distribution
            ArrayList<Double> probRes = new ArrayList<>();
            double sumAll = 0;
            for (int i = 0; i < tors.size(); i++) {
                double curProb = pareto.draw();
                sumAll += curProb;
                probRes.add(curProb);
            }

            // Normalize the "probability mass" by the total sum of "probability mass",
            // such that the results is a normalized Pareto distribution
            for (int i = 0; i < probRes.size(); i++) {
                probRes.set(i, probRes.get(i) / sumAll);
            }
            Collections.sort(probRes);

            double n = (double) tors.size();

            double torExcluded = 0.0;
            for (int i = 0; i < tors.size(); i++) {
                torExcluded += probRes.get(i) * probRes.get(i);
            }

            // Write away to random pair generator the pairs and their respective probability
            double counter = 0;
            for (int i = 0; i < tors.size(); i++) {
                for (int j = 0; j < tors.size(); j++) {
                    if (i != j) {

                        double torPairProb = probRes.get(i) * probRes.get(j) / (1 - torExcluded);
                        List<Integer> srcServers = new ArrayList<>(result.getRight().getServersOfTor(tors.get(i)));
                        List<Integer> dstServers = new ArrayList<>(result.getRight().getServersOfTor(tors.get(j))); // Simulator.getConfiguration().getGraphDetails()

                        double serverProb = torPairProb / (srcServers.size() * dstServers.size());
                        for (int src : srcServers) {
                            for (int dst : dstServers) {
                                //System.out.println(serverProb + "" + new ImmutablePair<>(src, dst));
                                counter += serverProb;
                            }
                        }

                    }
                }
            }
            //System.out.println(counter);
            //System.out.println(c2);
            //System.out.println(" done.");
            System.out.println(shape + "\n=======");
            System.out.println("Top 20 ToRs:");
            for (int i = tors.size() - 1; i >= Math.max(0, tors.size() - 20); i--) {
                System.out.println("ToR #" + tors.get(i) + " has probability " + probRes.get(i));
            }
           // System.out.println();
            System.out.println(shape + "\t" + probRes.get(tors.size() - 1));

        }


    }

}
