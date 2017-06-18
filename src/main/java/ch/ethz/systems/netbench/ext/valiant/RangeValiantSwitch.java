package ch.ethz.systems.netbench.ext.valiant;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.core.network.TransportLayer;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;

import java.util.HashSet;
import java.util.Set;

public class RangeValiantSwitch extends ValiantEcmpSwitch {

    // Which flows this switch has already seen (for statistical purposes)
    private static Set<Long> flowSeen = new HashSet<>();

    // Lower bound (inclusive) of valiant range
    protected final int lowBoundValiantRangeIncl;

    // Upper bound (inclusive) of valiant range
    protected final int highBoundValiantRangeIncl;

    // Size of the valiant range; size = upper - lower + 1
    private final int valiantRangeSize;

    /**
     * Constructor for Random Valiant ECMP switch.
     *
     * @param identifier        Network device identifier
     * @param transportLayer    Underlying server transport layer instance (set null, if none)
     * @param n                 Number of network devices in the entire network (for routing table size)
     * @param intermediary      Flowlet intermediary instance (takes care of hash adaptation for flowlet support)
     * @param lowBoundValiantRangeIncl  Lower bound (inclusive) of the range that determines which nodes can be valiant nodes
     * @param highBoundValiantRangeIncl Higher bound (inclusive) of the range that determines which nodes can be valiant nodes
     */
    public RangeValiantSwitch(int identifier, TransportLayer transportLayer, int n, Intermediary intermediary, int lowBoundValiantRangeIncl, int highBoundValiantRangeIncl) {
        super(identifier, transportLayer, n, intermediary);
        this.lowBoundValiantRangeIncl = lowBoundValiantRangeIncl;
        this.highBoundValiantRangeIncl = highBoundValiantRangeIncl;
        this.valiantRangeSize = this.highBoundValiantRangeIncl - this.lowBoundValiantRangeIncl + 1;
    }

    /**
     * Receives a TCP packet from the transport layer, which
     * is oblivious to the valiant ECMP routing happening underneath.
     * The TCP packet is then encapsulated to carry information of the
     * valiant routing. The choice of valiant node is done randomly
     * in the given range using the non-sequential hash.
     *
     * @param genericPacket     TCP packet instance
     */
    @Override
    public void receiveFromIntermediary(Packet genericPacket) {
        TcpPacket packet = (TcpPacket) genericPacket;
        assert(this.identifier == packet.getSourceId());

        // Determine source and destination ToR
        int sourceToR;
        int destinationToR;
        if (isWithinExtendedTopology) {
            sourceToR = Simulator.getConfiguration().getGraphDetails().getTorIdOfServer(packet.getSourceId());
            destinationToR = Simulator.getConfiguration().getGraphDetails().getTorIdOfServer(packet.getDestinationId());
        } else {
            sourceToR =  packet.getSourceId();
            destinationToR = packet.getDestinationId();
        }

        // Choose a valiant node (tries again if it finds one that is not allowed)
        int chosen;
        int i = 0;
        do {
            chosen = lowBoundValiantRangeIncl + packet.getHash(this.identifier, i) % valiantRangeSize;

            i++;
            if (i > 10000) {
                throw new RuntimeException(
                        "Chose a wrong random number more than a 10000 times. This is extremely unlikely to happen; " +
                        "presumably there is an extremely poor hashing function at work."
                );
            }
        } while (chosen == sourceToR || chosen == destinationToR);

        // Log for statistic of valiant balancing
        if (!flowSeen.contains(packet.getFlowId())) {
            SimulationLogger.increaseStatisticCounter("VAL_NODE_" + chosen);
            flowSeen.add(packet.getFlowId());
        }

        // Create encapsulation
        ValiantEncapsulation encapsulation = new ValiantEncapsulation(
                packet,
                chosen
        );

        // If it is a data acknowledgment, we always go shortest path
        // This excludes the third part of the three-way handshake, which is supposed to precede the initial data window
        if (packet.isSYN() || (packet.isACK() && packet.getAcknowledgementNumber() != 1)) {
            encapsulation.markPassedValiant();
        }

        // Propagate through the network
        receiveEncapsulationPassOn(encapsulation);

    }

    /**
     * Once the valiant switch has created an encapsulation, it needs to be
     * passed on to the network part of the switch which in term forwards it.
     *
     * This function is added such that the encapsulation can be altered by a possible extension.
     *
     * @param encapsulation     Valiant encapsulation instance
     */
    protected void receiveEncapsulationPassOn(ValiantEncapsulation encapsulation) {
        receive(encapsulation);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n\nRangeValiantEcmpSwitch<id=" + getIdentifier() + ", connected=" + connectedTo + ",\nrouting:\n");
        for (int i = 0; i < destinationToNextSwitch.size(); i++) {
            builder.append("\tfor " + i + " next hops are "  + destinationToNextSwitch.get(i) + "\n");
        }
        builder.append(",\ninclusive valiant range: [" + lowBoundValiantRangeIncl + ", " + highBoundValiantRangeIncl + "]\n");
        builder.append(">\n\n");
        return builder.toString();
    }

}