package ch.ethz.systems.netbench.ext.trafficpair;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.core.run.traffic.TrafficPlanner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrafficPairPlanner extends TrafficPlanner {

    private final long flowSizeByte;

    private String fileName;
    private List<TrafficPair> trafficPairs;
    private final boolean useFile;

    /**
     * Constructor.
     *
     * File should have the following structure:
     * <pre>
     *     [srcId] [dstId]
     *     [srcId] [dstId]
     *     ...
     *     [srcId] [dstId]
     * </pre>
     *
     * @param trafficPairsFileName  File name of the traffic pairs file
     */
    public TrafficPairPlanner(Map<Integer, TransportLayer> idToTransportLayerMap, String trafficPairsFileName, long flowSizeByte) {
        super (idToTransportLayerMap);
        this.fileName = trafficPairsFileName;
        this.useFile = true;
        SimulationLogger.logInfo("Flow planner", "TRAFFIC_PAIR_FILE");
        this.flowSizeByte = flowSizeByte;
    }

    /**
     * Constructor.
     *
     * @param trafficPairs  Traffic pairs
     */
    public TrafficPairPlanner(Map<Integer, TransportLayer> idToTransportLayerMap, List<TrafficPair> trafficPairs, long flowSizeByte) {
        super (idToTransportLayerMap);
        this.trafficPairs = trafficPairs;
        this.useFile = false;
        this.flowSizeByte = flowSizeByte;
        SimulationLogger.logInfo("Flow planner", "TRAFFIC_PAIR_LIST(flowSizeByte=" + flowSizeByte + ", pairs=" + trafficPairs + ")");
    }

    @Override
    public void createPlan(long durationNs) {
        if (useFile) {
            createPlanFromFile();
        } else {
            createPlanFromPairList();
        }
    }

    /**
     * Create planning by reading in pairs from file.
     */
    private void createPlanFromFile() {

        try {

            // Open input stream
            FileInputStream fileStream = new FileInputStream(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fileStream));

            // Simply read in the node pairs
            String strLine;
            while ((strLine = br.readLine()) != null) {

                // Split up sentence
                String[] match = strLine.split(" ");
                int srcId = Integer.valueOf(match[0]);
                int dstId = Integer.valueOf(match[1]);

                // Register the flow immediately
                registerFlow(0, srcId, dstId, flowSizeByte);

            }

            // Close input stream
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Create planning from pair list given.
     */
    private void createPlanFromPairList() {
        for (TrafficPair pair : trafficPairs) {
            registerFlow(0, pair.getFrom(), pair.getTo(), flowSizeByte);
        }
    }



    public static class TrafficPair {

        private final int from;
        private final int to;

        public TrafficPair(int from, int to) {
            this.from = from;
            this.to = to;
        }

        public int getFrom() {
            return from;
        }

        public int getTo() {
            return to;
        }

        public String toString() {
            return "(" + from + ", " + to + ")";
        }

    }

    /**
     * Generate all-to-one traffic pairs.
     *
     * @param n         Total number of nodes
     * @param target    Target traffic pair
     *
     * @return  Traffic pair list
     */
    public static List<TrafficPair> generateAllToOne(int n, int target) {

        // For each other server, create pair to target
        ArrayList<TrafficPair> ls = new ArrayList<>();
        for (int from = 0; from < n; from++) {
            if (from != target) {
                ls.add(new TrafficPair(from, target));
            }
        }

        return ls;

    }

    /**
     * Generate stride traffic pairs.
     *
     * @param n         Total number of nodes
     * @param stride    Stride
     *
     * @return  Traffic pair list
     */
    public static List<TrafficPair> generateStride(int n, int stride) {

        // For each other server, create pair to target
        ArrayList<TrafficPair> ls = new ArrayList<>();
        for (int from = 0; from < n; from++) {
             ls.add(new TrafficPair(from, (from + stride) % n));
        }

        return ls;

    }

    /**
     * Generate all-to-all traffic pairs.
     *
     * @param n         Total number of nodes
     *
     * @return  Traffic pair list
     */
    public static List<TrafficPair> generateAllToAll(int n) {

        // For each other server, create pair to target
        ArrayList<TrafficPair> ls = new ArrayList<>();
        for (Integer i : Simulator.getConfiguration().getGraphDetails().getServerNodeIds()) {
            for (Integer j : Simulator.getConfiguration().getGraphDetails().getServerNodeIds()) {
                if (!i.equals(j)) {
                    ls.add(new TrafficPair(i, j));
                }
            }
        }

        return ls;

    }

    /**
     * Generate single traffic pairs of src-dst.
     *
     * @param src   Source network device identifier
     * @param dst   Target network device identifier
     *
     * @return  Traffic pair list of size 1
     */
    public static List<TrafficPair> generateOneToOne(int src, int dst) {
        ArrayList<TrafficPair> ls = new ArrayList<>();
        ls.add(new TrafficPair(src, dst));
        return ls;
    }

}
