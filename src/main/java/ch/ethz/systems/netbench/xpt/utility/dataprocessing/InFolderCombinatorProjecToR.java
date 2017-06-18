package ch.ethz.systems.netbench.xpt.utility.dataprocessing;

import java.io.*;
import java.util.*;

public class InFolderCombinatorProjecToR {

    private static final boolean FOR_THESIS  = false;

    public static void main(String[] args) throws IOException {
        System.out.println(process("J:\\thesisrunmonday\\projector"));
    }
    public static String process(String main_folder) throws IOException {

        String final_result = "";
        if (!FOR_THESIS) {
            final_result += ("Name\t#Dropped\t#Resends\t(Resend/dropped)\tFlowlet Out-of-Order\tAverage FCT (ms)\t99th FCT (ms)\t99.9th FCT (ms)\tFl. compl.\tAverage FCT (ms)\t99th FCT (ms)\t99.9th FCT (ms)\tFl. compl. <= 100KB flows\tAverage FCT (ms)\t99th FCT (ms)\t99.9th FCT (ms)\tFl. compl. >= 10MB flows\tAvg. throughput flows >= 10MB (Gbit/s)\t99th of avg. throughput of flows >= 10MB (Gbit/s)\tMean non-server port utilization (%)\t99th non-server port utilization (%)\t99.9th non-server port utilization (%)\tMax. non-server port utilization (%)\tMean server port utilization (%)\t99th server port utilization (%)\t99.9th server port utilization (%)\tMax. server port utilization (%)\tMean nonzero server port utilization (%)\t99th nonzero server port utilization (%)\t99.9th nonzero server port utilization (%)\tMax. nonzero server port utilization (%)\n");
            //final_result += ("Name\tAverage FCT (ms)\t99th FCT (ms)\t99.9th FCT (ms)\tFl. compl.\tTot. all flows\tAverage FCT (micros)\t99th FCT (micros)\t99.9th FCT (micros)\tFl. compl.\tTot. <= 100KB flows\tAverage FCT (ms)\t99th FCT (ms)\t99.9th FCT (ms)\tFl. compl.\tTot <= 10MB flows\tAvg. throughput flows >= 10MB (Gbit/s)\t99th of avg. throughput of flows >= 10MB (Gbit/s)\tMean non-server port utilization (%)\t99th non-server port utilization (%)\t99.9th non-server port utilization (%)\tMax. non-server port utilization (%)\tMean server port utilization (%)\t99th server port utilization (%)\t99.9th server port utilization (%)\tMax. server port utilization (%)\n");
        }

        File fileA = new File( main_folder );
        File[] directories = fileA.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        ArrayList<File> directoryFiles = new ArrayList<>();
        Collections.addAll(directoryFiles, directories);
        Collections.sort(directoryFiles, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                String n1 = o1.getName();
                String n2 = o2.getName();
                if (n1.equals("multi-analysis")) {
                    return -1;
                } else if (n2.equals("multi-analysis")) {
                    return 1;
                }
                //Double frac1 = Double.valueOf(n1.split("fraction_")[1].split("_")[0]);
                //Double frac2 = Double.valueOf(n2.split("fraction_")[1].split("_")[0]);
                Integer flows1 = Integer.valueOf(n1.split("flows_")[1].split("_")[0]);
                Integer flows2 = Integer.valueOf(n2.split("flows_")[1].split("_")[0]);
                int a = 0; // frac1.compareTo(frac2);
                if (a == 0) {
                    return flows1.compareTo(flows2);
                } else {
                    return a;
                }
            }
        });

        int i = 0;
        for (File f : directoryFiles) {
            if (f.getName().equals("multi-analysis")) {
                continue;
            }
            String dir = f.getAbsolutePath();
            File file = new File(dir + "/plots/general_statistics.info");
            File file2 = new File(dir + "/statistics.log");

            HashMap<String, Double> values = new HashMap<>();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                String[] spl = line.split("\t");
                String key = spl[0].replace("\"", "");
                assert(values.get(key) == null);
                values.put(key, Double.valueOf(spl[1]));
                line = reader.readLine();
            }
            reader.close();

            BufferedReader reader2 = new BufferedReader(new FileReader(file2));
            line = reader2.readLine();
            while (line != null) {
                String[] spl = line.split(": ");
                assert(values.get(spl[0]) == null);
                values.put(spl[0], Double.valueOf(spl[1]));
                line = reader2.readLine();
            }
            reader2.close();

            if (toName(f.getName()) != null) {
                if (!FOR_THESIS) {
                    final_result += f.getName() + "\t" + convertToNiceRow2(values) + "\n";
                } else {
                    final_result += toName(f.getName()) + " & " + convertToNiceThesisRow(values) + "\n";
                }
            }
            i++;

        }

        return final_result;

    }

    private static String toName(String s) {
        /*if (s.startsWith("normal_fat_tree_f")) {
            return "FT (100\\%)";
        } else if (s.startsWith("oversub_fat_tree_50_f")) {
            return "FT (50\\%)";
        } else if (s.startsWith("xpander_n256_d12_fr")) {
            return "XP (ECMP)";
        } else if (s.startsWith("xpander_n256_d12_vlb")) {
            return "XP (VLB)";
        } else if (s.startsWith("xpander_n256_d12_ecmp_then_")) {
            return "XP (HYB)";
        } else {
            return null;
        }*/
        return s;
    }

    private static String roundIt(double d) {
        if (d >= 1000) {
            return String.valueOf(Long.valueOf(Math.round(d)));
        } else if (d >= 10) {
            return String.valueOf(Math.round(d * 10.0) / 10.0);
        } else if (d >= 1) {
            return String.valueOf(Math.round(d * 100.0) / 100.0);
        } else {
            return String.valueOf(Math.round(d * 1000.0) / 1000.0);
        }
    }

    public static String convertToNiceThesisRow(Map<String, Double> values) {

        return
                roundIt(values.get("fct_all_mean")) + " & " +
                roundIt(values.get("fct_all_99th")) + " & " +
                roundIt(values.get("fct_100KB_mean")) + " & " +
                roundIt(values.get("fct_100KB_99th"))+ " & " +
                roundIt(values.get("fct_greater_than_10MB_mean")) + " & " +
                roundIt(values.get("fct_greater_than_10MB_99th")) + " & " +
                roundIt((values.get("flows_all_finished_count") / values.get("flows_all_count"))* 100) + "\\% \\\\ \\hline "
        ;
    }

    public static String convertToNiceRow2(Map<String, Double> values) {

        return
                        values.get("PACKETS_DROPPED") + "\t" +
                        values.get("TCP_RESEND_OCCURRED") + "\t" +
                        ((values.get("TCP_RESEND_OCCURRED") == null || values.get("PACKETS_DROPPED") == null) ? "UNDEF" : (values.get("TCP_RESEND_OCCURRED") / values.get("PACKETS_DROPPED") * 100)) + "%\t" +
                        values.get("TCP_FLOWLET_OUT_OF_ORDER") + "\t" +
                        values.get("fct_all_mean") + "\t" +
                        values.get("fct_all_99th") + "\t" +
                        values.get("fct_all_99_9th") + "\t" +
                        (values.get("flows_all_finished_count") / values.get("flows_all_count") * 100) + "%\t" +
                        values.get("fct_100KB_mean") + "\t" +
                        values.get("fct_100KB_99th") + "\t" +
                        values.get("fct_100KB_99_9th") + "\t" +
                        (values.get("flows_100KB_finished_count") / values.get("flows_100KB_count") * 100) + "%\t" +
                        values.get("fct_greater_than_10MB_mean") + "\t" +
                        values.get("fct_greater_than_10MB_99th") + "\t" +
                        values.get("fct_greater_than_10MB_99_9th") + "\t" +
                        (values.get("flows_greater_than_10MB_finished_count") / values.get("flows_greater_than_10MB_count") * 100) + "%\t" +
                        values.get("throughput_mean_over_10MB_throughputs") + "\t" +
                        values.get("throughput_99th_over_10MB_throughputs") + "\t" +
                        values.get("nonserver_port_utilization_mean") + "\t" +
                        values.get("nonserver_port_utilization_99th") + "\t" +
                        values.get("nonserver_port_utilization_99_9th") + "\t" +
                        values.get("nonserver_port_utilization_max") + "\t" +
                        values.get("server_port_utilization_mean") + "\t" +
                        values.get("server_port_utilization_99th") + "\t" +
                        values.get("server_port_utilization_99_9th") + "\t" +
                        values.get("server_port_utilization_max") + "\t" +
                        values.get("nonzero_server_port_utilization_mean") + "\t" +
                        values.get("nonzero_server_port_utilization_99th") + "\t" +
                        values.get("nonzero_server_port_utilization_99_9th") + "\t" +
                        values.get("nonzero_server_port_utilization_max")
        ;
    }

}
