package ch.ethz.systems.netbench.xpt.utility;

public class FTEcmpFC {

    public static void main(String args[]) {

        System.out.println("#server_pair_id,src,dst,pdf_num_bytes");
        int c = 0;
        for (int i = 320; i < 333; i++) {
            if (i > 324 && i < 328) {
                continue;
            }
            for (int j = 320; j < 333; j++) {
                if (j > 324 && j < 328) {
                    continue;
                }
                if (i == j) {
                    continue;
                }
                System.out.println(c + "," + i + "," + j + "," + (1.0/90.0));
                c++;
            }
        }

    }
}
