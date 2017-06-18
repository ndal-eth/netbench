package ch.ethz.systems.netbench.xpt.utility;

import ch.ethz.systems.netbench.core.config.GraphDetails;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NormalizeIt {

    public static void main(String[] args) throws IOException {

        // Open file stream
        FileReader input = new FileReader("private/plots/projector/projector_mean_fct_ms_norm.txt");
        BufferedReader br = new BufferedReader(input);


        // Go over parameter lines one-by-one, stop when encountering non-parameter lines
        String line;
        Map<Integer, Double> flowsToNorm = new HashMap<>();

        while ((line = br.readLine()) != null) {
            line = line.trim();

            String[] spl = line.split("\t");
            Integer flows = Integer.valueOf(spl[1]);
            Double value = Double.valueOf(spl[2]);

            if (spl[0].contains("with_servers")) {
                if (spl[0].equals("xpander_n128_d16_hybrid_with_servers_thres_2434900")) {
                    flowsToNorm.put(flows, value);
                }
            }
            System.out.println(spl[0] + "\t" + flows + "\t" + (value / flowsToNorm.get(flows)));


        }

        br.close();

    }

}
