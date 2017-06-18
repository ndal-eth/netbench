package ch.ethz.systems.netbench.xpt.utility;

import java.util.ArrayList;
import java.util.List;

public class HashTestMain {

    public static void main(String args[]) {
        List<Integer> counters =new ArrayList<Integer>();
        counters.add(0);
        counters.add(0);
        counters.add(0);
        counters.add(0);
        counters.add(0);

        for (int i = 0; i < 400000; i++) {
            int h = hash(i) % 5;
            counters.set(h, counters.get(h) + 1);
        }

        for (int c = 0; c < 5; c++) {
            System.out.println(c + ": " + counters.get(c));
        }
    }

    public static int hash(int a) {
        a = (a+0x7ed55d16) + (a<<12);
        a = (a^0xc761c23c) ^ (a>>19);
        a = (a+0x165667b1) + (a<<5);
        a = (a+0xd3a2646c) ^ (a<<9);
        a = (a+0xfd7046c5) + (a<<3);
        a = (a^0xb55a4f09) ^ (a>>16);
        a = Math.abs(a);
        return a;
    }

}
