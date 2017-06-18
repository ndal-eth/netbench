package ch.ethz.systems.netbench.xpt.utility.dataprocessing;

import java.io.*;
import java.util.*;

public class SingularCombinatorParetoGraph {

    public static String OUTPUT_FOLDER;

    /*public static void main(String args[]) throws IOException {
        String fraction = "0.19"; // 0.025 0.05 0.19
        OUTPUT_FOLDER = "L:\\plotdata\\pareto_graph\\" + fraction;
        process("L:\\thesisrunmonday\\pareto_graph\\100KB_shape_1.05\\" + fraction, "normal_fat_tree_f");
        process("L:\\thesisrunmonday\\pareto_graph\\100KB_shape_1.05\\" + fraction, "normal_fat_tree_unordered_f");
        process("L:\\thesisrunmonday\\pareto_graph\\100KB_shape_1.05\\" + fraction, "oversub_fat_tree_50_f");
        process("L:\\thesisrunmonday\\pareto_graph\\100KB_shape_1.05\\" + fraction, "oversub_fat_tree_50_unordered_f");
        process("L:\\thesisrunmonday\\pareto_graph\\100KB_shape_1.05\\" + fraction, "xpander_n256_d12_ecmp_then_vlb_f");
        process("L:\\thesisrunmonday\\pareto_graph\\100KB_shape_1.05\\" + fraction, "xpander_n256_d12_f");
        process("L:\\thesisrunmonday\\pareto_graph\\100KB_shape_1.05\\" + fraction, "xpander_n256_d12_vlb_f");
    }*/

    /*public static void main(String args[]) throws IOException {
        String fraction = "0.19"; // 0.025 0.05 0.19
        OUTPUT_FOLDER = "L:\\plotdata\\identity_alizadeh_graph\\" + fraction;
        process("L:\\thesisrunmonday\\identity_alizadeh_graph\\" + fraction, "normal_fat_tree_f");
        process("L:\\thesisrunmonday\\identity_alizadeh_graph\\" + fraction, "normal_fat_tree_unordered_f");
        process("L:\\thesisrunmonday\\identity_alizadeh_graph\\" + fraction, "oversub_fat_tree_50_f");
        process("L:\\thesisrunmonday\\identity_alizadeh_graph\\" + fraction, "oversub_fat_tree_50_unordered_f");
        process("L:\\thesisrunmonday\\identity_alizadeh_graph\\" + fraction, "xpander_n256_d12_ecmp_then_vlb_f");
        process("L:\\thesisrunmonday\\identity_alizadeh_graph\\" + fraction, "xpander_n256_d12_f");
        process("L:\\thesisrunmonday\\identity_alizadeh_graph\\" + fraction, "xpander_n256_d12_vlb_f");
    }*/

    /*public static void main(String args[]) throws IOException {
        String fraction = "0.05"; // 0.025 0.05 0.19
        OUTPUT_FOLDER = "L:\\plotdata\\alizadeh_graph\\" + fraction;
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + fraction, "normal_fat_tree_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + fraction, "normal_fat_tree_unordered_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + fraction, "oversub_fat_tree_50_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + fraction, "oversub_fat_tree_50_unordered_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + fraction, "xpander_n256_d12_ecmp_then_vlb_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + fraction, "xpander_n256_d12_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + fraction, "xpander_n256_d12_vlb_f");
    }*/

    /*public static void main(String args[]) throws IOException {
        String fraction = "0.19"; // 0.025 0.05 0.19
        OUTPUT_FOLDER = "L:\\plotdata\\alizadeh_graph\\" + fraction;
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + fraction + "_n216", "xpander_n216_d11_ecmp_then_vlb_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + fraction + "_n216", "xpander_n216_d11_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + fraction + "_n216", "xpander_n216_d11_vlb_f");
    }*/

    /*public static void main(String args[]) throws IOException {
        String fraction = "0.04";
        OUTPUT_FOLDER = "L:\\plotdata\\alizadeh_graph\\" + fraction;
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + fraction + "_n216", "xpander_n216_d11_ecmp_then_vlb_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + fraction + "_n216", "xpander_n216_d11_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + fraction + "_n216", "xpander_n216_d11_vlb_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + fraction, "normal_fat_tree_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + fraction, "normal_fat_tree_unordered_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + fraction, "oversub_fat_tree_50_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + fraction, "oversub_fat_tree_50_unordered_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + fraction, "xpander_n256_d12_ecmp_then_vlb_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + fraction, "xpander_n256_d12_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + fraction, "xpander_n256_d12_vlb_f");
    }*/


    /*public static void main(String args[]) throws IOException {
        OUTPUT_FOLDER = "L:\\plotdata\\alizadeh_graph\\0.186_0.196";
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + "0.186" + "_n216", "xpander_n216_d11_ecmp_then_vlb_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + "0.186" + "_n216", "xpander_n216_d11_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + "0.186" + "_n216", "xpander_n216_d11_vlb_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + "0.196", "normal_fat_tree_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + "0.196", "normal_fat_tree_unordered_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + "0.196", "oversub_fat_tree_50_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + "0.196", "oversub_fat_tree_50_unordered_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + "0.196", "xpander_n256_d12_ecmp_then_vlb_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + "0.196", "xpander_n256_d12_f");
        process("L:\\thesisrunmonday\\alizadeh_graph\\" + "0.196", "xpander_n256_d12_vlb_f");
    }*/

    public static void main(String args[]) throws IOException {
        int x = 400;
        OUTPUT_FOLDER = "L:\\plotdata\\oversubopt\\" + x;
        process("L:\\thesisrunmonday\\oversubopt\\" + x + "_n216", "xpander_n216_d11_f");
        process("L:\\thesisrunmonday\\oversubopt\\" + x + "_n216", "xpander_n216_d11_vlb_f");
        process("L:\\thesisrunmonday\\oversubopt\\" + x, "normal_fat_tree_unordered_f");
        process("L:\\thesisrunmonday\\oversubopt\\" + x, "xpander_n256_d12_f");
        process("L:\\thesisrunmonday\\oversubopt\\" + x, "xpander_n256_d12_vlb_f");
    }


   /* public static void main(String args[]) throws IOException {
        String skew = "0.97"; // 2
        OUTPUT_FOLDER = "L:\\plotdata\\alizadeh_skew_graph\\" + skew;
        process("L:\\thesisrunmonday\\alizadeh_skew_graph\\" + skew, "normal_fat_tree_s");
        process("L:\\thesisrunmonday\\alizadeh_skew_graph\\" + skew, "oversub_fat_tree_50_s");
        process("L:\\thesisrunmonday\\alizadeh_skew_graph\\" + skew, "xpander_n256_d12_ecmp_then_vlb_s");
        process("L:\\thesisrunmonday\\alizadeh_skew_graph\\" + skew, "xpander_n256_d12_s");
        process("L:\\thesisrunmonday\\alizadeh_skew_graph\\" + skew, "xpander_n256_d12_vlb_s");
    }*/

    /*

        public static void main(String args[]) throws IOException {
        String skew = "2"; // 2
        OUTPUT_FOLDER = "L:\\plotdata\\alizadeh_skew_graph\\" + skew;
        process("L:\\thesisrunmonday\\alizadeh_skew_graph\\" + skew, "normal_fat_tree_s");
        process("L:\\thesisrunmonday\\alizadeh_skew_graph\\" + skew, "oversub_fat_tree_50_s");
        process("L:\\thesisrunmonday\\alizadeh_skew_graph\\" + skew, "xpander_n256_d12_ecmp_then_vlb_s");
        process("L:\\thesisrunmonday\\alizadeh_skew_graph\\" + skew, "xpander_n256_d12_ecmp_then_vlb_thresh_10000000_s");
        process("L:\\thesisrunmonday\\alizadeh_skew_graph\\" + skew, "xpander_n256_d12_ecmp_then_vlb_again_thresh_2500000_s");
        process("L:\\thesisrunmonday\\alizadeh_skew_graph\\" + skew, "xpander_n256_d12_s");
        process("L:\\thesisrunmonday\\alizadeh_skew_graph\\" + skew, "xpander_n256_d12_vlb_s");
        process("L:\\thesisrunmonday\\alizadeh_skew_graph\\" + skew, "xpander_n216_d11_ecmp_then_vlb_s");
        process("L:\\thesisrunmonday\\alizadeh_skew_graph\\" + skew, "xpander_n216_d11_s");
        process("L:\\thesisrunmonday\\alizadeh_skew_graph\\" + skew, "xpander_n216_d11_vlb_s");
    }
     */

    /*public static void main(String args[]) throws IOException {
        String frac = "0.7"; // 0.025 0.05
        OUTPUT_FOLDER = "L:\\plotdata\\alizadeh_graph_server\\" + frac;
        process("L:\\thesisrunmonday\\alizadeh_graph_server\\" + frac, "normal_fat_tree_f");
        process("L:\\thesisrunmonday\\alizadeh_graph_server\\" + frac, "oversub_fat_tree_50_f");
        process("L:\\thesisrunmonday\\alizadeh_graph_server\\" + frac, "xpander_n256_d12_ecmp_then_vlb_f");
        process("L:\\thesisrunmonday\\alizadeh_graph_server\\" + frac, "xpander_n256_d12_f");
        process("L:\\thesisrunmonday\\alizadeh_graph_server\\" + frac, "xpander_n256_d12_vlb_f");
    }*/

    public static String process(String main_folder, String start) throws IOException {

        String final_result = "";
        Integer[] rates;
        rates = new Integer[]{
                //50000, 100000, 200000, 300000, 400000, 500000
                //1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000
                //10000, 20000, 30000, 40000, 50000, 60000, 70000, 80000, 90000, 100000, 200000, 300000, 400000, 500000
                10000, 20000, 30000, 40000, 50000, 60000, 70000, 80000, 90000, 100000, 110000, 120000, 130000, 140000, 150000, 160000, 200000, 300000, 400000, 500000
                //2000, 4000, 6000, 8000, 10000, 12000, 14000, 16000, 18000, 20000
                //2000, 5000, 8000, 11000, 14000, 17000, 20000, 23000, 26000, 29000
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
            System.out.println(dir);
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

        FileWriter writer = new FileWriter(OUTPUT_FOLDER + "\\" + start.substring(0, start.length() - 1) + "f.txt");
        writer.write(final_result);
        writer.close();

        return final_result;

    }

    private static double getVal(Map<String, Double> values, String s) {
        if (values.get(s) == null) {
            return 0;
        } else {
            return values.get(s);
        }
    }

    public static String convertToNiceRow3(Map<String, Double> values) {

        return
/*2*/                        values.get("fct_all_mean") + "\t" +
/*3*/                        values.get("fct_all_99th") + "\t" +
/*4*/                        values.get("fct_all_99_9th") + "\t" +
/*5*/                        (values.get("flows_all_finished_count") / values.get("flows_all_count") * 100) + "%\t" +
/*6*/                        values.get("fct_100KB_mean") + "\t" +
/*7*/                        values.get("fct_100KB_99th") + "\t" +
/*8*/                        values.get("fct_100KB_99_9th") + "\t" +
/*9*/                        (values.get("flows_100KB_finished_count") / values.get("flows_100KB_count") * 100) + "%\t" +
/*10*/                       getVal(values, "fct_greater_than_10MB_mean") + "\t" +
/*11*/                       getVal(values, "fct_greater_than_10MB_99th") + "\t" +
/*12*/                       getVal(values, "fct_greater_than_10MB_99_9th") + "\t" +
/*13*/                       (getVal(values, "flows_greater_than_10MB_finished_count") / ((getVal(values, "flows_greater_than_10MB_count") == 0 ? 1 : getVal(values, "flows_greater_than_10MB_count"))) * 100) + "%\t" +
/*14*/                       values.get("throughput_mean_over_10MB_throughputs") + "\t" +
/*15*/                       values.get("throughput_99th_over_10MB_throughputs") + "\t" +
/*16*/                       values.get("nonserver_port_utilization_mean") + "\t" +
/*17*/                       values.get("nonserver_port_utilization_99th") + "\t" +
/*18*/                       values.get("nonserver_port_utilization_99_9th") + "\t" +
/*19*/                       values.get("nonserver_port_utilization_max") + "\t" +
/*20*/                       values.get("server_port_utilization_mean") + "\t" +
/*21*/                       values.get("server_port_utilization_99th") + "\t" +
/*22*/                       values.get("server_port_utilization_99_9th") + "\t" +
/*23*/                       values.get("server_port_utilization_max") + "\t" +
/*24*/                       values.get("nonzero_server_port_utilization_mean") + "\t" +
/*25*/                       values.get("nonzero_server_port_utilization_99th") + "\t" +
/*26*/                       values.get("nonzero_server_port_utilization_99_9th") + "\t" +
/*27*/                       values.get("nonzero_server_port_utilization_max")
                ;
    }

}
