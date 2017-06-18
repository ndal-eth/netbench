package ch.ethz.systems.netbench.xpt.utility;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ExcelCombinator {

    public static void main(String[] args) throws IOException {

        File file = new File("J:\\flw_1000ns");
        File[] directories = file.listFiles(new FilenameFilter() {
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
                return o1.getName().compareTo(o2.getName());
            }
        });

        String result = "";
        for (File f : directoryFiles) {
            String dir = f.getAbsolutePath();
            BufferedReader br = new BufferedReader(new FileReader(dir + "/plots/general_statistics_excel_row.txt"));
            result += f.getName() + "\t" + br.readLine() + "\n";
            br.close();
        }

        System.out.println(result);

    }

}
