package ch.ethz.systems.netbench.xpt.utility.dataprocessing;

import java.io.*;
import java.util.*;

public class SingularCombinatorProjector {

    public static String OUTPUT_FOLDER = "L:\\plotdata\\projector";

    public static void main(String args[]) throws IOException {
        process("L:\\thesisrunmonday\\projector", "normal_fat_tree_flows");
        process("L:\\thesisrunmonday\\projector", "normal_fat_tree_with_servers");
        process("L:\\thesisrunmonday\\projector", "xpander_n128_d16_ecmp_flows");
        process("L:\\thesisrunmonday\\projector", "xpander_n128_d16_ecmp_with_servers");
        process("L:\\thesisrunmonday\\projector", "xpander_n128_d16_vlb_flows");
        process("L:\\thesisrunmonday\\projector", "xpander_n128_d16_vlb_with_servers");
    }

    public static String process(String main_folder, String start) throws IOException {

        String final_result = "";
        Integer[] rates;
        rates = new Integer[]{
                2000, 4000, 6000, 8000, 10000, 12000, 14000
        };

        File fileA = new File( main_folder);
        File[] directories = fileA.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });


        ArrayList<File> directoryFiles2 = new ArrayList<>();
        Collections.addAll(directoryFiles2, directories);

        ArrayList<File> directoryFiles = new ArrayList<>();
        for (File f : directoryFiles2) {
            if (f.getName().startsWith(start)) {
                directoryFiles.add(f);
            }
        }

        Collections.sort(directoryFiles, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                String[] spl = o1.getName().split("flows")[1].split("_");
                String[] spl2 = o2.getName().split("flows")[1].split("_");
                int a = Integer.valueOf(spl[1]).compareTo(Integer.valueOf(spl2[1]));
                if (a == 0) {
                    return o1.getName().compareTo(o2.getName());
                } else {
                    return a;
                }
            }
        });

        int i = 0;
        for (File f : directoryFiles) {
            String dir = f.getAbsolutePath();
            File file = new File(dir + "/plots/general_statistics.info");

            HashMap<String, Double> values = new HashMap<>();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                String[] spl = line.split("\t");
                String key = spl[0].replace("\"", "");
                Double current = values.get(key);
                if (current == null) {
                    values.put(key, Double.valueOf(spl[1]));
                } else {
                    values.put(key, current + Double.valueOf(spl[1]));
                }
                line = reader.readLine();
            }
            reader.close();
            final_result += rates[i] + "\t" + convertToNiceRow3(values) + "\n";
            i++;

        }

        FileWriter writer = new FileWriter(OUTPUT_FOLDER + "\\projector_" + start + ".txt");
        writer.write(final_result);
        writer.close();

        return final_result;

    }

    public static String convertToNiceRow3(Map<String, Double> values) {

        return
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
