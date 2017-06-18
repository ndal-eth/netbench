package ch.ethz.systems.netbench.xpt.utility;

/**
 * Created by Zimon on 23/01/2017.
 */
public class TestMain {

    public static void main(String args[]) {

        int c = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                System.out.println(c + "," + (216 + i) + "," + (728 + j) + "," + (1.0 / 64));
                c++;
            }

        }

    }

}
