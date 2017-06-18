package ch.ethz.systems.netbench.xpt.utility;

import java.util.ArrayList;
import java.util.List;

public class TwoSequenceHashTest {

    public static void main(String args[]) {

        int[] bases = new int[]{33, 55};

        int outcomes = 6;
        int sequenceLength = 2000;

        // Initialize counters
        List<Integer> counters = new ArrayList<>();
        List<List<Integer>> sequences = new ArrayList<>();
        for (int i = 0; i < outcomes; i++) {
            counters.add(0);
            sequences.add(new ArrayList<Integer>());
        }

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < sequenceLength; j++) {
                int h = hash(hash(bases[i]) + j) % outcomes;
                counters.set(h, counters.get(h) + 1);
                sequences.get(i).add(h);
            }
        }

        // Calculate overlap
        int same = 0;
        for (int i = 0; i < sequenceLength; i++) {
            if (sequences.get(0).get(i).equals(sequences.get(1).get(i))) {
                same++;
            }
        }
        System.out.println("Sequences overlap for " + (((double) same) / sequenceLength * 100) + "% which should be close to the expected " + (1.0 / outcomes * 100) + "%");

        System.out.println("Sum of outcomes for both sequences: ");
        for (int c = 0; c < outcomes; c++) {
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
