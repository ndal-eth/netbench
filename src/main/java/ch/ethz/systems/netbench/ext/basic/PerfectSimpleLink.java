package ch.ethz.systems.netbench.ext.basic;

import ch.ethz.systems.netbench.core.network.Link;

public class PerfectSimpleLink extends Link {

    private final long delayNs;
    private final long bandwidthBitPerNs;

    /**
     * Perfect simple link that never drops a packet.
     *
     * @param delayNs               Delay of each packet in nanoseconds
     * @param bandwidthBitPerNs     Bandwidth of the link (maximum line rate) in bits/ns
     */
    PerfectSimpleLink(long delayNs, long bandwidthBitPerNs) {
        this.delayNs = delayNs;
        this.bandwidthBitPerNs = bandwidthBitPerNs;
    }

    @Override
    public long getDelayNs() {
        return delayNs;
    }

    @Override
    public long getBandwidthBitPerNs() {
        return bandwidthBitPerNs;
    }

    @Override
    public boolean doesNextTransmissionFail(long packetSizeBits) {
        return false;
    }

}
