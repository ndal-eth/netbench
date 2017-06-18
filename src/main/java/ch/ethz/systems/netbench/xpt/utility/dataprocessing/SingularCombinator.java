package ch.ethz.systems.netbench.xpt.utility.dataprocessing;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class SingularCombinator {

    public static String process(String main_folder, String routing) throws IOException {
        return process(main_folder, routing, false);
    }
    public static String process(String main_folder, String routing, boolean not30000) throws IOException {

        String final_result = "";
        Integer[] rates;
        if (not30000) {
            rates = new Integer[]{
                    2000, 4000, 6000, 8000, 10000, 12000, 14000, 16000, 18000, 20000
            };
        } else {
            rates = new Integer[]{
                    2000, 4000, 6000, 8000, 10000, 12000, 14000, 16000, 18000, 20000, 30000
            };
        }

        File fileA = new File( main_folder + "\\" + routing);
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
                String[] spl = o1.getName().split("rate")[1].split("_");
                String[] spl2 = o2.getName().split("rate")[1].split("_");
                int a = Integer.valueOf(spl[0]).compareTo(Integer.valueOf(spl2[0]));
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
            final_result += rates[i] + "\t" + CalculateAverageStatistics.convertToNiceRow(values) + "\n";
            i++;

        }

        return final_result;

    }

}
