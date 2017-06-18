package ch.ethz.systems.netbench.xpt.utility.dataprocessing;

import java.io.FileWriter;
import java.io.IOException;

public class ProcessAll {

    public static void main(String args[]) throws IOException
    {

        String outputFolder = "C:\\temp";

        String base = "J:\\processed\\";

        ////////////////////
        // XPANDER

        System.out.println("Xpander FL gap...");

        String[] xpanderFLGapMainFolders = new String[] {
                "xpander_n128_ecmpfirst_flgap\\d8",
                "xpander_n128_with_servers_ecmpfirst_flgap\\d8",
                "xpander_n128_ecmpfirst_flgap\\d16",
                "xpander_n128_with_servers_ecmpfirst_flgap\\d16"
        };

        String[] xpanderFLGapRoutings = new String[] {
                "vlb_xyz_ecmpfirst_flgap0",
                "vlb_xyz_ecmpfirst_flgap1",
                "vlb_xyz_ecmpfirst_flgap15000",
                "vlb_xyz_ecmpfirst_flgap30000",
                "vlb_xyz_ecmpfirst_flgap40000",
                "vlb_xyz_ecmpfirst_flgap50000",
                "vlb_xyz_ecmpfirst_flgap60000",
                "vlb_xyz_ecmpfirst_flgap80000",
                "vlb_xyz_ecmpfirst_flgap100000",
                "vlb_xyz_ecmpfirst_flgap120000"
        };

        for (String mainFolder : xpanderFLGapMainFolders) {
            for (String routing : xpanderFLGapRoutings) {
                String fileName = mainFolder.replace("\\", "_") + "_" + routing;
                String res = SingularCombinator.process(base + mainFolder, routing, true);
                FileWriter writer = new FileWriter(outputFolder + "\\" + fileName + ".txt");
                writer.write(res);
                writer.close();
            }
        }

        ////////////////////
        // FAT TREE

        System.out.println("Fat-tree...");

        // HEADER
        // System.out.println("\tAll flows\t\t\t\t\t<= 1MB flows\t\t\t\t\t<= 10MB flows\t\t\t\t\t\t\t\t\t\t\t\t\t\t");
        // System.out.println("Load\tAverage FCT (ms)\t99th FCT (ms)\t99.9th FCT (ms)\tFl. compl.\tTot. all flows\tAverage FCT (ms)\t99th FCT (ms)\t99.9th FCT (ms)\tFl. compl.\tTot. <= 1MB flows\tAverage FCT (ms)\t99th FCT (ms)\t99.9th FCT (ms)\tFl. compl.\tTot <= 10MB flows\tAvg. throughput flows >= 10MB (Gbit/s)\t99th of avg. throughput of flows >= 10MB (Gbit/s)\tMean non-server port utilization (%)\t99th non-server port utilization (%)\t99.9th non-server port utilization (%)\tMax. non-server port utilization (%)\tMean server port utilization (%)\t99th server port utilization (%)\t99.9th server port utilization (%)\tMax. server port utilization (%)");

        String[] fatTreeMainFolders = new String[] {
                "fat_tree",
                "fat_tree_with_servers"
        };

        String[] fatTreeRoutings = new String[] {
                "identity",
                "ankit_uniform_dctcp",
                "uniform"
        };

        for (String mainFolder : fatTreeMainFolders) {
            for (String routing : fatTreeRoutings) {
                String fileName = mainFolder + "_" + routing;
                String res = SingularCombinator.process(base + mainFolder, routing);
                FileWriter writer = new FileWriter(outputFolder + "\\" + fileName + ".txt");
                writer.write(res);
                writer.close();
            }
        }

        ////////////////////
        // XPANDER

        System.out.println("Xpander...");

        String[] xpanderMainFolders = new String[] {
                "xpander_n128\\d8",
                "xpander_n128_with_servers\\d8",
                "xpander_n128\\d16",
                "xpander_n128_with_servers\\d16"
        };

        String[] xpanderRoutings = new String[] {
                "ecmp_uniform",
                "vlb_identity",
                "vlb_ankit_uniform_dctcp",
                "vlb_uniform",
                "vlb_xyz_ecmpfirst"
        };

        for (String mainFolder : xpanderMainFolders) {
            for (String routing : xpanderRoutings) {
                String fileName = mainFolder.replace("\\", "_") + "_" + routing;
                String res = SingularCombinator.process(base + mainFolder, routing);
                FileWriter writer = new FileWriter(outputFolder + "\\" + fileName + ".txt");
                writer.write(res);
                writer.close();

            }
        }

        ////////////////////
        // JELLYFISH 10 SEEDS

        System.out.println("Jellyfish N128 D8/D16...");

        String[] jellyfishRoutings = new String[] {
                "ecmp_uniform",
                "vlb_identity",
                "vlb_ankit_uniform_dctcp",
                "vlb_uniform",
                "vlb_xyz_ecmpfirst"
        };

        String[] jellyfishMainFolders = new String[] {
                "jellyfish_n128\\d8",
                "jellyfish_n128_with_servers\\d8",
                "jellyfish_n128\\d16",
                "jellyfish_n128_with_servers\\d16"
        };

        String[] jellyfishPrefixes = new String[] {
                "jellyfish_n128_d8_seed_",
                "jellyfish_n128_d8_seed_",
                "jellyfish_n128_d16_seed_",
                "jellyfish_n128_d16_seed_"
        };

        for (int i = 0; i < 4; i++) {
            System.out.print(i + "/4...");
            String mainFolder = jellyfishMainFolders[i];
            String prefix = jellyfishPrefixes[i];
            for (String routing : jellyfishRoutings) {
                //System.out.println(routing + "...");
                String fileName = mainFolder.replace("\\", "_") + "_" + routing;
                String res = CalculateAverageStatistics.process(base + mainFolder, prefix, routing);
                FileWriter writer = new FileWriter(outputFolder + "\\" + fileName + ".txt");
                writer.write(res);
                writer.close();
            }
            System.out.println(" done.");
        }

        // Main Folder: J:\processed\jellyfish_n128_with_servers\d8
        // Prefix: jellyfish_n128_d8_seed_

        ////////////////////
        // JELLYFISH N206 10 SEEDS

        System.out.println("Jellyfish N206 D11...");

        String[] jellyfishRoutingsN206 = new String[] {
                "ecmp_uniform",
                "vlb_identity",
                "vlb_ankit_uniform_dctcp",
                "vlb_uniform",
                "vlb_xyz_ecmpfirst"
        };

        String mainFolder = "jellyfish_n206_d11";
        String prefix = "jellyfish_n206_d11_seed_";
        for (String routing : jellyfishRoutingsN206) {
            System.out.print(routing + "...");
            String fileName = mainFolder.replace("\\", "_") + "_" + routing;
            String res = CalculateAverageStatistics.process(base + mainFolder, prefix, routing);
            FileWriter writer = new FileWriter(outputFolder + "\\" + fileName + ".txt");
            writer.write(res);
            writer.close();
            System.out.println(" done.");
        }

    }

}
