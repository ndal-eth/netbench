package ch.ethz.systems.netbench.ext.valiant;

import ch.ethz.systems.netbench.ext.basic.IpPacket;
import ch.ethz.systems.netbench.ext.basic.TcpPacket;

public class ValiantEncapsulation extends IpPacket implements ValiantEncapsulationHeader {

    private final TcpPacket packet;
    private final int valiantDestination;
    private boolean passedValiant;

    ValiantEncapsulation(TcpPacket packet, int valiantDestination) {
        super(packet.getFlowId(), packet.getSizeBit() - 480L, packet.getSourceId(), packet.getDestinationId(), packet.getTTL());
        this.packet = packet;
        this.valiantDestination = valiantDestination;
        this.passedValiant = false;
    }

    @Override
    public TcpPacket getPacket() {
        return packet;
    }

    @Override
    public int getValiantDestination() {
        return valiantDestination;
    }

    @Override
    public void markPassedValiant() {
        passedValiant = true;
    }

    @Override
    public boolean passedValiant() {
        return passedValiant;
    }

    @Override
    public void markCongestionEncountered() {
        this.packet.markCongestionEncountered();
    }

}
