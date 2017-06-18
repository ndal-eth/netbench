package ch.ethz.systems.netbench.xpt.utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Main utility for checking if there is a difference between two files,
 * and if so, show the file index (1-based) where the first difference is.
 */
public class FileLineComparator {

    public static void main(String[] args) {

        try {

            // Open two file streams
            BufferedReader br1 = new BufferedReader(new FileReader("C:/path/to/file/a.txt"));
            BufferedReader br2 = new BufferedReader(new FileReader("C:/path/to/file/b.txt"));

            // Read in others
            int i = 1;
            while (true) {
                String lineA = br1.readLine();
                String lineB = br2.readLine();

                if (lineA == null && lineB == null) {
                    System.out.println("Line comparator found no difference between the two files.");
                    break;
                }

                if (lineA == null || lineB == null) {
                    System.out.println("[" + i + "]: " + lineA + " vs. " + lineB);
                    break;
                }

                if (!lineA.equals(lineB)) {
                    System.out.println("[" + i + "]: " + lineA + " vs. " + lineB);
                    break;
                }
                i++;

            }

            // Close file stream
            br1.close();
            br2.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
