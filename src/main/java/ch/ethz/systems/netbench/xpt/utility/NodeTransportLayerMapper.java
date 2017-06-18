package ch.ethz.systems.netbench.xpt.utility;

/**
 * Main utility tool to generate the node list whether a node has a transport
 * layer in a scenario topology file.
 */
public class NodeTransportLayerMapper {

    public static void main(String[] args) {

        int n = 100;
        for (int i = 0; i < n; i++) {
            if (matchCriteria(i)) {
                System.out.println(i + " 1");
            } else {
                System.out.println(i + " 0");
            }
        }

    }

    private static boolean matchCriteria(int i) {
        return i < 80;
    }

}
