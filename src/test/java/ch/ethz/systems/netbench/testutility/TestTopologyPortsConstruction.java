package ch.ethz.systems.netbench.testutility;

import ch.ethz.systems.netbench.core.network.OutputPort;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestTopologyPortsConstruction {

    private Map<Pair<Integer, Integer>, OutputPort> ports;

    public TestTopologyPortsConstruction(String linksText) {

        // Read in all ports
        ports = new HashMap<>();
        String[] linkSplits = linksText.split(",");
        for (String i : linkSplits) {

            String[] spl = i.split("-");
            int a = Integer.valueOf(spl[0]);
            int b = Integer.valueOf(spl[1]);

            OutputPort portAtoB = mock(OutputPort.class);
            when(portAtoB.getOwnId()).thenReturn(a);
            when(portAtoB.getTargetId()).thenReturn(b);

            OutputPort portBtoA = mock(OutputPort.class);
            when(portBtoA.getOwnId()).thenReturn(b);
            when(portBtoA.getTargetId()).thenReturn(a);

            ports.put(new ImmutablePair<>(a, b), portAtoB);
            ports.put(new ImmutablePair<>(b, a), portBtoA);

        }

    }

    public OutputPort getPort(int from, int to) {
        Pair<Integer, Integer> p = new ImmutablePair<>(from, to);
        OutputPort port = ports.get(p);
        if (port == null) {
            throw new RuntimeException("Port " + from + "->" + to + "does not exist.");
        }
        return port;
    }

}
