package ch.ethz.systems.netbench.xpt.utility.dataprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CalculateAverageStatistics {

    // Main Folder: J:\processed\jellyfish_n128_with_servers\d8
    // Prefix: jellyfish_n128_d8_seed_

    public static String process(String mainFolder, String prefix, String routing) throws IOException

    {

        String final_result = "";

        String location = mainFolder + "\\" + routing + "\\";
        String betweenfix = "_rate";
        String postfix = "_" + routing;

        Integer[] rates = new Integer[]{
                2000, 4000, 6000, 8000, 10000, 12000, 14000, 16000, 18000, 20000, 30000
        };

        Integer[] seeds = new Integer[]{
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9
        };

        for (Integer rate : rates) {

            Map<String, Double> values = new HashMap<>();

            for (Integer seed : seeds) {

                File file = new File(location + prefix + "" + seed + betweenfix + rate + "" + postfix + "/plots/general_statistics.info");
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();
                while (line != null) {
                    String[] spl = line.split("\t");
                    String key = spl[0].replace("\"", "");
                    Double current = values.get(key);
                    //if (key.equals("fct_all_mean")) {
                    //    System.out.println(Double.valueOf(spl[1]));
                    //}
                    if (current == null) {
                        values.put(key, Double.valueOf(spl[1]));
                    } else {
                        values.put(key, current + Double.valueOf(spl[1]));
                    }
                    line = reader.readLine();
                }
                reader.close();

            }

            for (String key : values.keySet()) {
                values.put(key, values.get(key) / 10.0);
            }

            final_result += rate + "\t" + convertToNiceRow(values) + "\n";

        }

        return final_result;

    }

    public static String convertToNiceRow(Map<String, Double> values) {

        return
                values.get("fct_all_mean") + "\t" +
                values.get("fct_all_99th") + "\t" +
                values.get("fct_all_99_9th") + "\t" +
                values.get("flows_all_finished_count") + "\t" +
                values.get("flows_all_count") + "\t" +
                values.get("fct_100KB_mean") * 1000 + "\t" +
                values.get("fct_100KB_99th") * 1000 + "\t" +
                values.get("fct_100KB_99_9th") * 1000 + "\t" +
                values.get("flows_100KB_finished_count") + "\t" +
                values.get("flows_100KB_count") + "\t" +
                values.get("fct_10MB_mean") + "\t" +
                values.get("fct_10MB_99th") + "\t" +
                values.get("fct_10MB_99_9th") + "\t" +
                values.get("flows_10MB_finished_count") + "\t" +
                values.get("flows_10MB_count") + "\t" +
                values.get("throughput_mean_over_10MB_throughputs") + "\t" +
                values.get("throughput_99th_over_10MB_throughputs") + "\t" +
                values.get("nonserver_port_utilization_mean") + "\t" +
                values.get("nonserver_port_utilization_99th") + "\t" +
                values.get("nonserver_port_utilization_99_9th") + "\t" +
                values.get("nonserver_port_utilization_max") + "\t" +
                values.get("server_port_utilization_mean") + "\t" +
                values.get("server_port_utilization_99th") + "\t" +
                values.get("server_port_utilization_99_9th") + "\t" +
                values.get("server_port_utilization_max")
        ;
    }

}
