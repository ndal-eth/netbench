package ch.ethz.systems.netbench.core.config;

import edu.asu.emit.algorithm.graph.Graph;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static ch.ethz.systems.netbench.core.config.GraphReaderTest.createSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class TopologyServerExtenderTest {

    @Test
    public void testMiniFatTreeExtension() throws IOException {

        // Graph:
        //
        //     0    1
        //   2  3  4  5
        //

        File inFile = constructGraph(
                6, 16,
                "incl_range(2,5)", "set(0,1)", "incl_range(2,5)",
                "2 0\n0 2\n2 1\n1 2\n3 0\n0 3\n3 1\n1 3\n4 0\n0 4\n4 1\n1 4\n5 0\n0 5\n5 1\n1 5"
        );

        // Perform extension
        File tempOut = File.createTempFile("topology", ".tmp");
        TopologyServerExtender extender = new TopologyServerExtender(inFile.getAbsolutePath(), tempOut.getAbsolutePath());
        extender.extendRegular(7);
        assertTrue(inFile.delete());

        Pair<Graph, GraphDetails> res = GraphReader.read(tempOut.getAbsolutePath());
        GraphDetails details = res.getRight();

        // Graph dimensions
        assertEquals(34, details.getNumNodes()); // 6 + 4*7 = 34
        assertEquals(72, details.getNumEdges()); // 16 + 2*4*7 = 16 + 56 = 72

        // Sets
        assertTrue(details.isAutoExtended());
        assertEquals(createSet(2, 3, 4, 5), details.getTorNodeIds()); // ToRs remain the same
        assertEquals(createSet(0, 1), details.getSwitchNodeIds()); // Switches remain the same
        Set<Integer> servers = new HashSet<>();
        for (int i = 6; i < 34; i++ ) {
            servers.add(i);
        }
        assertEquals(servers, details.getServerNodeIds()); // Servers are added

        // Test saved mapping from server perspective
        for (int i = 6; i < 34; i++ ) {
            assertEquals((int) details.getTorIdOfServer(i), 2 + (int) Math.floor((double) (i - 6) / 7.0));
        }

        // Test saved mapping from ToR perspective
        for (int i = 2; i <= 5; i++ ) {
            Set<Integer> torServers = new HashSet<>();
            for (int j = 6 + (i - 2) * 7; j < 6 + (i - 1) * 7; j++ ) {
                torServers.add(j);
            }
            assertEquals(torServers, details.getServersOfTor(i));
        }

        assertTrue(tempOut.delete());

    }

    public File constructGraph(
            int numNodes,
            int numEdges,
            String servers,
            String switches,
            String tors,
            String edges
    ) throws IOException {

        // Create temporary files
        File tempTopology = File.createTempFile("topology", ".tmp");

        // Write temporary config file
        BufferedWriter topologyWriter = new BufferedWriter(new FileWriter(tempTopology));
        topologyWriter.write("# A comment line followed by a white line\n\n");
        topologyWriter.write("|V|=" + numNodes + "\n");
        topologyWriter.write("|E|=" + numEdges + "\n");
        topologyWriter.write("Servers=" + servers + "\n");
        topologyWriter.write("Switches=" + switches + "\n");
        topologyWriter.write("ToRs=" + tors + "\n");
        topologyWriter.write(edges);
        topologyWriter.close();

        return tempTopology;

    }

}
