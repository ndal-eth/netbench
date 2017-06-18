package ch.ethz.systems.netbench.core.config;

import edu.asu.emit.algorithm.graph.Graph;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraphReader {

    /**
     * Read in a graph and its details.
     *
     * File structure (numbers at end separated by space are the edges):
     *
     * # Details (comment)
     * |V|=320
     * |E|=1280
     * Servers=COLLECTION
     * Switches=COLLECTION
     * ToRs=COLLECTION
     * AutoExtended=true
     *
     * # Comment line
     * 0 1
     * 1 1
     * 2 1
     * 1 2
     * ...
     *
     * With COLLECTION either e.g. set(2, 3, 4, 5) or incl_range(2, 5)
     *
     *
     * @param fileName  File name
     *
     * @return Graph and its details
     */
    public static Pair<Graph, GraphDetails> read(String fileName) {

        try {

            // Calculate SHA-1 hash
            FileInputStream fis = new FileInputStream(new File(fileName));
            String sha1 = DigestUtils.sha1Hex(fis);
            fis.close();

            // Open file stream
            FileReader input = new FileReader(fileName);
            BufferedReader br = new BufferedReader(input);

            // Mandatory parameters
            GraphDetails details = new GraphDetails(sha1);

            // Go over parameter lines one-by-one, stop when encountering non-parameter lines
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();

                // Skip empty or commented lines
                if (line.length() == 0 || line.startsWith("#")) {
                    continue;
                }

                // First check for parameters
                int index = line.indexOf("=");
                if (index != -1) {

                    String key = line.substring(0, index);
                    String val = line.substring(index + 1);
                    switch (key) {

                        // |V|=NUM_VERTICES
                        case "|V|":
                            details.setNumNodes(Integer.valueOf(val));
                            break;

                        // |E|=NUM_EDGES
                        case "|E|":
                            details.setNumEdges(Integer.valueOf(val));
                            break;

                        // AutoExtended=true
                        case "AutoExtended":
                            if (val.equals("true")) {
                                details.setIsAutoExtended();
                            } else {
                                throw new IllegalArgumentException("Cannot have value different than 'true' for AutoExtended");
                            }
                            break;

                        // Servers=set(IDENTIFIERS SEPARATED BY COMMA)
                        // Servers=incl_range(LB_INCL, HB_INCL)
                        case "Servers":
                            details.setServerNodeIds(convertCollectionOfIds(val));
                            break;

                        // Switches=set(IDENTIFIERS SEPARATED BY COMMA)
                        // Switches=incl_range(LB_INCL, HB_INCL)
                        case "Switches":
                            details.setSwitchNodeIds(convertCollectionOfIds(val));
                            break;

                        // ToRs=set(IDENTIFIERS SEPARATED BY COMMA)
                        // ToRs=incl_range(LB_INCL, HB_INCL)
                        case "ToRs":
                            details.setTorNodeIds(convertCollectionOfIds(val));
                            break;

                        default:
                            throw new IllegalArgumentException("Unauthorized key in file: " + key);
                    }

                } else {
                    break;
                }

            }

            br.close();

            // Check all necessary arguments
            if (details.getNumNodes() == -1 || details.getNumEdges() == -1 || details.getServerNodeIds() == null
                    || details.getTorNodeIds() == null || details.getSwitchNodeIds() == null) {
                throw new IllegalArgumentException("One of the mandatory keys is missing: |V|, |E|, Servers, Switches, ToRs");
            }

            // ToRs cannot overlap with switches
            Set<Integer> torSwitchIntersection = new HashSet<>(details.getTorNodeIds());
            torSwitchIntersection.retainAll(details.getSwitchNodeIds());
            if (torSwitchIntersection.size() > 0) {
                throw new IllegalArgumentException("Tors overlap with switches: not allowed.");
            }

            // Switches cannot overlap with servers
            Set<Integer> switchServerIntersection = new HashSet<>(details.getSwitchNodeIds());
            switchServerIntersection.retainAll(details.getServerNodeIds());
            if (switchServerIntersection.size() > 0) {
                throw new IllegalArgumentException("Switches overlap with servers: not allowed.");
            }

            // The join of all must cover all nodes
            Set<Integer> allJoin = new HashSet<>(details.getTorNodeIds());
            allJoin.addAll(details.getSwitchNodeIds());
            allJoin.addAll(details.getServerNodeIds());
            if (allJoin.size() != details.getNumNodes()) {
                throw new IllegalArgumentException("The ToRs, switches and servers together must cover the entire topology.");
            }

            // Either all ToRs overlap with all servers, or none
            Set<Integer> torServerIntersection = new HashSet<>(details.getTorNodeIds());
            torServerIntersection.retainAll(details.getServerNodeIds());
            if (torServerIntersection.size() != 0 && !details.getTorNodeIds().equals(details.getServerNodeIds())) {
                throw new IllegalArgumentException("Either all ToRs are servers, or no ToRs are servers.");
            }

            // There must be at least two servers
            if (details.getNumServers() < 2) {
                throw new IllegalArgumentException("There are less than two servers (namely " + details.getNumServers() + "): communication is not possible in such a topology.");
            }

            // Go over link lines one-by-one, skipping parameter lines
            input = new FileReader(fileName);
            br = new BufferedReader(input);
            List<Pair<Integer, Integer>> linkDirectedPairs = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                line = line.trim();

                // Skip empty or commented lines
                if (line.length() == 0 || line.startsWith("#")) {
                    continue;
                }

                // Check links
                int index = line.indexOf("=");
                if (index == -1) {
                    String[] spl = line.split(" ");
                    int srcId = Integer.valueOf(spl[0]);
                    int dstId = Integer.valueOf(spl[1]);
                    linkDirectedPairs.add(new ImmutablePair<>(srcId, dstId));

                    // Save the coupling of ToR to its servers
                    if (details.getTorNodeIds().contains(srcId)
                            && details.getServerNodeIds().contains(dstId)
                            && !details.getTorNodeIds().contains(dstId)) {
                        details.saveTorHasServer(srcId, dstId);
                    }

                }

            }

            // Close file stream
            br.close();

            // Check number of edges
            if (details.getNumEdges() != linkDirectedPairs.size()) {
                throw new IllegalArgumentException("The number of edges mentioned does not correspond to number of edges mentioned.");
            }

            // Create graph
            Graph graph = new Graph(details.getNumNodes(), linkDirectedPairs);

            // Return final instantiated network
            return new ImmutablePair<>(graph, details);

        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid file: " + fileName + "; I/O error: " + e.getCause());
        }

    }

    /**
     * Converts a collection (e.g. set(INDICES), or incl_range(LOW, HIGH) into a list of values.
     *
     * @param val   String value
     *
     * @return List of values
     */
    private static Set<Integer> convertCollectionOfIds(String val) {

        // Result
        Set<Integer> result = new HashSet<>();

        // Set value, e.g. ToRs=set(3, 4, 1, 99, 3)
        if (val.startsWith("set(") && val.endsWith(")")) {
            String inner = val.substring(4, val.length() - 1);
            if (inner.length() > 0) {
                String[] spl = inner.split(",");
                for (String s : spl) {
                    if (result.contains(Integer.valueOf(s.trim()))) {
                        throw new IllegalArgumentException("Duplicate in set value.");
                    }
                    result.add(Integer.valueOf(s.trim()));
                }
            }

        // Range value, e.g. ToRs=incl_range(0, 127)
        } else if (val.startsWith("incl_range(") && val.endsWith(")")) {
            String inner = val.substring(11, val.length() - 1);
            String[] spl = inner.split(",");
            int low = Integer.valueOf(spl[0].trim());
            int high = Integer.valueOf(spl[1].trim());
            if (high < low) {
                throw new IllegalArgumentException("Range high is lower than range low.");
            }
            for (int i = low; i <= high; i++) {
                result.add(i);
            }

        } else {
            throw new IllegalArgumentException("Invalid value for a collection: " + val);
        }

        return result;

    }

}
