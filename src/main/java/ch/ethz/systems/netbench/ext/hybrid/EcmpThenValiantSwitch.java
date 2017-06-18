package ch.ethz.systems.netbench.ext.hybrid;

import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.ext.valiant.RangeValiantSwitch;
import ch.ethz.systems.netbench.ext.valiant.ValiantEncapsulation;
import ch.ethz.systems.netbench.core.network.TransportLayer;

import java.util.HashMap;
import java.util.Map;

public class EcmpThenValiantSwitch extends RangeValiantSwitch {

    // Amount of flow size sent out for each flow
    private final Map<Long, Long> flowSizeSent;

    // Threshold at which the switch goes from direct ECMP routing to valiant routing
    private final long switchThresholdBytes;

    /**
     * Constructor for Hybrid switch.
     *
     * @param identifier        Network device identifier
     * @param transportLayer    Underlying server transport layer instance (set null, if none)
     * @param n                 Number of network devices in the entire network (for routing table size)
     * @param intermediary      Flowlet intermediary instance (takes care of hash adaptation for flowlet support)
     * @param lowBoundValiantRangeIncl  Lower bound (inclusive) of the range that determines which nodes can be valiant nodes
     * @param highBoundValiantRangeIncl Higher bound (inclusive) of the range that determines which nodes can be valiant nodes
     * @param switchThresholdBytes      Number of bytes a flow needs to send out before it goes for valiant instead of ECMP
     */
    public EcmpThenValiantSwitch(int identifier, TransportLayer transportLayer, int n, Intermediary intermediary, int lowBoundValiantRangeIncl, int highBoundValiantRangeIncl, long switchThresholdBytes) {
        super(identifier, transportLayer, n, intermediary, lowBoundValiantRangeIncl, highBoundValiantRangeIncl);
        this.flowSizeSent = new HashMap<>();
        this.switchThresholdBytes = switchThresholdBytes;
    }

    @Override
    protected void receiveEncapsulationPassOn(ValiantEncapsulation encapsulation) {

        // Determine amount of flow already dispatched
        Long amount = flowSizeSent.get(encapsulation.getFlowId());
        amount = amount == null ? 0L : amount;
        amount += encapsulation.getPacket().getDataSizeByte();
        flowSizeSent.put(encapsulation.getFlowId(), amount);

        // If past threshold, then we start valiant things
        if (amount <= switchThresholdBytes) {
            encapsulation.markPassedValiant();
        }

        // Send to network
        receive(encapsulation);

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n\nEcmpThenValiantSwitch<switchThreshold=" + switchThresholdBytes + ", id=" + getIdentifier() + ", connected=" + connectedTo + ",\nrouting:\n");
        for (int i = 0; i < destinationToNextSwitch.size(); i++) {
            builder.append("\tfor " + i + " next hops are "  + destinationToNextSwitch.get(i) + "\n");
        }
        builder.append(",\ninclusive valiant range: [" + lowBoundValiantRangeIncl + ", " + highBoundValiantRangeIncl + "]\n");
        builder.append(">\n\n");
        return builder.toString();
    }

}