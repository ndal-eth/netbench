package ch.ethz.systems.netbench.xpt.utility;

import ch.ethz.systems.netbench.core.config.GraphDetails;
import ch.ethz.systems.netbench.core.config.GraphReader;
import ch.ethz.systems.netbench.xpt.sourcerouting.SourceRoutingPath;
import ch.ethz.systems.netbench.xpt.sourcerouting.SourceRoutingSwitch;
import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Vertex;
import edu.asu.emit.algorithm.graph.algorithms.YenTopKShortestPathsAlg;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NumberShortestPathsCheck {

    public static void main(String args[]) throws IOException {

        GraphReader reader = new GraphReader();
        Pair<Graph, GraphDetails> res = GraphReader.read("private/data/topologies/xpander_n216_d11.topology");
        Graph graph = res.getLeft();
        int numNodes = graph.getVertexList().size();
        int k = 10;

        // Open output file stream
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter("temp/numb_shortest_paths_eval.txt"));

        // Go over every network device pair and set the forwarder switch routing table
        int i = 27;
        int j = 196;
        //for (int i = 0; i < numNodes; i++) {
        //    for (int j = 0; j < numNodes; j++) {

                if (i != j) {

                    // Find shortest paths as many wanted
                    YenTopKShortestPathsAlg alg = new YenTopKShortestPathsAlg(graph, graph.getVertex(i), graph.getVertex(j));
                    int shortestPathLength = -1;
                    List<List<Pair<Integer, Integer>>> paths  = new ArrayList<>();

                    while(alg.hasNext())
                    {

                        // Translate path
                        SourceRoutingPath path = new SourceRoutingPath();
                        for (Vertex v : alg.next().getVertexList()) {
                            path.add(v.getId());
                        }

                        List<Pair<Integer, Integer>> edgePath = new ArrayList<>();
                        for (int b = 1; b < path.size(); b++) {
                            edgePath.add(new ImmutablePair<>(path.get(b - 1), path.get(b)));
                        }

                        if (shortestPathLength == -1) {
                            shortestPathLength = path.size() - 1;
                        }

                        if (path.size() - 1 != shortestPathLength) {
                            break;
                        } else {
                            paths.add(edgePath);
                        }

                    }

                    System.out.println(paths.size());
                    System.out.println(paths);

                    List<List<Pair<Integer, Integer>>> nonConflictingPaths  = new ArrayList<>();
                    for (List<Pair<Integer, Integer>> p1 : paths) {
                        assert(p1.size() == shortestPathLength);

                        boolean conflict = false;
                        for (List<Pair<Integer, Integer>> p2 : nonConflictingPaths) {
                            for (Pair<Integer, Integer> e : p2) {
                                if (p1.contains(e)) {
                                    conflict = true;
                                }
                            }
                        }
                        if (!conflict) {
                            nonConflictingPaths.add(p1);
                        }
                    }

                    //System.out.println(i + " - > " + j + " has " + found + "  paths.");
                    outputWriter.write(i + "\t" + j + "\t" + paths.size() + "\t" + shortestPathLength + "\t" + nonConflictingPaths.size() + "\n");



                    // Create warning message if not enough different paths could be found
                    //if (found != k) {
                    //    System.out.println("WARNING: could only find " + found + " paths for " + i + " -> " + j + ", which is less than k=" + k + ".");
                    //}

                }
            //}

            // Log progress...
            //if (numNodes > 10 && ((i + 1) % Math.ceil((numNodes / 100.0))) == 0) {
            //    System.out.print(" " + (((double) i + 1) / (numNodes) * 100) + "%...");
            //}

        //}

        outputWriter.close();

    }

}
