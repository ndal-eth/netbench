package ch.ethz.systems.netbench.xpt.utility;

import java.util.ArrayList;
import java.util.List;

public class SequenceUtility {

    /**
     * Retrieve a list of integers until the given number n.
     *
     * @param n     Righter outer exclusive bound
     *
     * @return      List [0, 1, 2, ... n - 1]
     */
    static List<Integer> allUntilExclusive(int n) {
        return allInInclusiveRange(0, n - 1);
    }

    /**
     * Retrieve all integers in a range.
     *
     * @param startIncl     Start bound (inclusive)
     * @param endIncl       End bound (inclusive)
     *
     * @return  List of integers [start, start + 1, ..., end - 1, end]
     */
    static List<Integer> allInInclusiveRange(int startIncl, int endIncl) {
        List<Integer> res = new ArrayList<>();
        for (int i = startIncl; i <= endIncl; i++) {
            res.add(i);
        }
        return res;
    }

}
