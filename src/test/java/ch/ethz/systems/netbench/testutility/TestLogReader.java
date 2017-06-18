package ch.ethz.systems.netbench.testutility;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestLogReader {

    public static Map<Long, FlowCompletionTuple> getFlowCompletionMapping(String runFolderDir) throws IOException {

        Map<Long, FlowCompletionTuple> mapping = new HashMap<>();

        // Open file stream
        FileReader input = new FileReader(runFolderDir + "/flow_completion.csv.log");
        BufferedReader br = new BufferedReader(input);

        // Go over parameter lines one-by-one, stop when encountering non-parameter lines
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            String spl[] = line.split(",");
            mapping.put(Long.valueOf(spl[0]),
                    new FlowCompletionTuple(
                            Long.valueOf(spl[0]),
                            Integer.valueOf(spl[1]),
                            Integer.valueOf(spl[2]),
                            Long.valueOf(spl[3]),
                            Long.valueOf(spl[4]),
                            Long.valueOf(spl[5]),
                            Long.valueOf(spl[6]),
                            Long.valueOf(spl[7]),
                            spl[8].equals("TRUE")
                    )
            );

        }

        return mapping;

    }

    public static Map<Pair<Integer, Integer>, PortUtilizationTuple> getPortUtilizationMapping(String runFolderDir) throws IOException {

        Map<Pair<Integer, Integer>, PortUtilizationTuple> mapping = new HashMap<>();

        // Open file stream
        FileReader input = new FileReader(runFolderDir + "/port_utilization.csv.log");
        BufferedReader br = new BufferedReader(input);

        // Go over parameter lines one-by-one, stop when encountering non-parameter lines
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            String spl[] = line.split(",");
            mapping.put(new ImmutablePair<>(Integer.valueOf(spl[0]), Integer.valueOf(spl[1])),
                    new PortUtilizationTuple(
                            Integer.valueOf(spl[0]),
                            Integer.valueOf(spl[1]),
                            spl[2].equals("Y"),
                            Long.valueOf(spl[3]),
                            Double.valueOf(spl[4])
                    )
            );

        }

        return mapping;

    }



    public static class FlowCompletionTuple {

        private final long flowId;
        private final int sourceId;
        private final int targetId;
        private final long sentBytes;
        private final long totalSizeBytes;
        private final long startTime;
        private final long endTime;
        private final long duration;
        private final boolean completed;

        FlowCompletionTuple(long flowId, int sourceId, int targetId, long sentBytes, long totalSizeBytes, long startTime, long endTime, long duration, boolean completed) {
            this.flowId = flowId;
            this.sourceId = sourceId;
            this.targetId = targetId;
            this.sentBytes = sentBytes;
            this.totalSizeBytes = totalSizeBytes;
            this.startTime = startTime;
            this.endTime = endTime;
            this.duration = duration;
            this.completed = completed;
        }

        public long getFlowId() {
            return flowId;
        }

        public int getSourceId() {
            return sourceId;
        }

        public int getTargetId() {
            return targetId;
        }

        public long getSentBytes() {
            return sentBytes;
        }

        public long getTotalSizeBytes() {
            return totalSizeBytes;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public long getDuration() {
            return duration;
        }

        public boolean isCompleted() {
            return completed;
        }

    }

    public static class PortUtilizationTuple {

        private final int ownId;
        private final int targetId;
        private final boolean attachedToServer;
        private final long utilizationNs;
        private final double utilizationPercentage;

        PortUtilizationTuple(int ownId, int targetId, boolean attachedToServer, long utilizationNs, double utilizationPercentage) {
            this.ownId = ownId;
            this.targetId = targetId;
            this.attachedToServer = attachedToServer;
            this.utilizationNs = utilizationNs;
            this.utilizationPercentage = utilizationPercentage;
        }

        public int getOwnId() {
            return ownId;
        }

        public int getTargetId() {
            return targetId;
        }

        public boolean isAttachedToServer() {
            return attachedToServer;
        }

        public long getUtilizationNs() {
            return utilizationNs;
        }

        public double getUtilizationPercentage() {
            return utilizationPercentage;
        }

    }

}
