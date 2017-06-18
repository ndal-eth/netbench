package ch.ethz.systems.netbench.ext.valiant;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.network.Intermediary;
import ch.ethz.systems.netbench.core.network.Packet;
import ch.ethz.systems.netbench.ext.ecmp.EcmpSwitch;
import ch.ethz.systems.netbench.core.network.TransportLayer;

import java.util.List;

abstract class ValiantEcmpSwitch extends EcmpSwitch {

    boolean isWithinExtendedTopology;

    /**
     * Constructor for abstract Valiant ECMP switch.
     *
     * @param identifier        Network device identifier
     * @param transportLayer    Underlying server transport layer instance (set null, if none)
     * @param n                 Number of network devices in the entire network (for routing table size)
     * @param intermediary      Flowlet intermediary instance (takes care of hash adaptation for flowlet support)
     */
    ValiantEcmpSwitch(int identifier, TransportLayer transportLayer, int n, Intermediary intermediary) {
        super(identifier, transportLayer, n, intermediary);
        isWithinExtendedTopology = Simulator.getConfiguration().isPropertyDefined("scenario_topology_extend_with_servers");
    }

    /**
     * Receive a packet to be networked. It makes sure that each encapsulation
     * has passed its valiant node, before being routed to the ultimate destination.
     *
     * @param genericPacket Valiant ECMP packet encapsulation instance
     */
    @Override
    public void receive(Packet genericPacket) {

        // Convert to encapsulation
        ValiantEncapsulationHeader encapsulation = (ValiantEncapsulationHeader) genericPacket;

        // Determine destination of encapsulated packet
        int actualDestination = encapsulation.getPacket().getDestinationId();
        int valiantDestination = encapsulation.getValiantDestination();

        // Determine destination to go to
        int destinationToGoTo = encapsulation.passedValiant() ? actualDestination : valiantDestination;

        // Check if it has arrived at the desired valiant node
        if (!encapsulation.passedValiant() && valiantDestination == this.identifier) {
            encapsulation.markPassedValiant();
            destinationToGoTo = actualDestination;
        }

        // Check if it has arrived
        if (actualDestination == this.identifier) {

            // Hand to the underlying server
            this.passToIntermediary(encapsulation.getPacket()); // Will throw null-pointer if this network device does not have a server attached to it

        } else {

            // If it is within an extended topology, it means that if it has the ability
            // to go the destination server, it should immediately
            if (isWithinExtendedTopology && this.connectedTo.contains(actualDestination)) {
                destinationToGoTo = actualDestination;
            }

            // Forward to the next switch
            List<Integer> possibilities = destinationToNextSwitch.get(destinationToGoTo);
            this.targetIdToOutputPort.get(possibilities.get(encapsulation.getPacket().getHash(this.identifier) % possibilities.size())).enqueue(genericPacket);

        }

    }

}