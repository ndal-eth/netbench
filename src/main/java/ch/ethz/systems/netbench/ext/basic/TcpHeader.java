package ch.ethz.systems.netbench.ext.basic;

public interface TcpHeader extends IpHeader {

    /**
     * Retrieve size of data transported by the packet in bytes.
     *
     * @return  Data in bytes
     */
    long getDataSizeByte();

    /**
     * Get the source port.
     *
     * @return  Source port
     */
    int getSourcePort();

    /**
     * Get the destination port.
     *
     * @return  Destination port
     */
    int getDestinationPort();

    /**
     * Get the sequence number of the packet.
     * The sequence number is the number of the first byte of the data carried.
     *
     * @return  Sequence number
     */
    long getSequenceNumber();

    /**
     * Get the acknowledgment number.
     *
     * @return  Acknowledgment number
     */
    long getAcknowledgementNumber();

    /**
     * Check whether the packet is ECN-nonce concealment protection.
     *
     * @return  True iff ECN-nonce concealment protection (NS)
     */
    boolean isNS();

    /**
     * Check whether the congestion window (CWR) is reduced (R).
     *
     * @return  True iff congestion window reduced (CWR)
     */
    boolean isCWR();

    /**
     * Check whether the packet this one acknowledges encountered congestion.
     *
     * @return  True iff echo congestion encountered (ECE)
     */
    boolean isECE();

    /**
     * Check whether packet marked as urgent.
     *
     * @return  True iff urgent (URG)
     */
    boolean isURG();

    /**
     * Check whether the packet is an acknowledgment
     * (as such, the {@link #getAcknowledgementNumber()}
     * has a meaningful value).
     *
     * @return  True iff acknowledgment (ACK)
     */
    boolean isACK();

    /**
     * Check whether the packet asks upon delivery to push
     * the data to the client.
     *
     * @return  True iff pushes data (PSH)
     */
    boolean isPSH();

    /**
     * Check whether the packet indicates a connection reset.
     *
     * @return  True iff connection reset (RST)
     */
    boolean isRST();

    /**
     * Check whether the packet serves as a synchronization of
     * sequence numbers between two sockets.
     *
     * @return  True iff connection synchronization (SYN)
     */
    boolean isSYN();

    /**
     * Check whether the packet indicates a finalization of
     * the connection.
     *
     * @return  True iff connection finish (FIN)
     */
    boolean isFIN();

    /**
     * Get the number of bytes that the receiver is currently
     * willing to accept. This should be treated as an upper
     * limit for the congestion window.
     *
     * @return  Maximum receiver window size in bytes
     */
    double getWindowSize();

    /**
     * Retrieve the hash for this packet with the extra factor
     * to make sure a different non-sequential hash can be made
     * for different purposes (e.g. different choice at switch A than at B).
     *
     * What the hash is dependent on besides the extra factor is determined
     * by which of the two you called before this function:
     * (a) {@link #setHashSrcDstFlowFlowletDependent()}
     * or
     * (b) {@link #setHashSrcDstFlowletDependent()}.
     *
     * @param extraFactor   Extra factor
     *
     * @return Hash value for this packet
     */
    default int getHash(int extraFactor) {
        return hash(extraFactor + getNonSequentialHash());
    }

    /**
     * Retrieve the non-sequential hash.
     * Should only be used internally.
     *
     * @return  Non-sequential hash
     */
    int getNonSequentialHash();

    /**
     * Set the non-sequential hash.
     * DO NOT USE EXTERNALLY, SHOULD ONLY
     * BE ACCESSED BY INTERFACE.
     *
     * After call must be guaranteed that {@link #getNonSequentialHash()}
     * will return the value set in this function.
     *
     * @param nonSeqHash    Non-sequential hash to set
     */
    void setNonSequentialHash(int nonSeqHash);

    /**
     * Retrieve the hash for this packet with two extra factors
     * to make sure a different non-sequential hash can be made
     * for different purposes (e.g. different choice at switch A than at B).
     *
     * What the hash is dependent on besides the extra factor is determined
     * by which of the two you called before this function:
     * (a) {@link #setHashSrcDstFlowFlowletDependent()}
     * or
     * (b) {@link #setHashSrcDstFlowletDependent()}.
     *
     * @param extraFactorA   Extra factor A
     * @param extraFactorB   Extra factor B
     *
     * @return Hash value for this packet
     */
    default int getHash(int extraFactorA, int extraFactorB) {
        return hash(extraFactorA + hash(extraFactorB + getNonSequentialHash()));
    }

    /**
     * Create a hash value which is dependent on the source, destination,
     * flow identifier and flowlet identifier of this packet.
     *
     * This means the following:
     * (a) The sequential hash: starting at non-sequential hash base based on (src, dst and flowId), it linearly adds
     *     the respective flowlet identifier.
     *
     * (b) The non-sequential hash: complete hash dependent on (src, dst, flowId, flowletId).
     */
    default void setHashSrcDstFlowFlowletDependent() {
        setNonSequentialHash(absolute(hash(this.getSourceId() + hash(this.getDestinationId() + hash((int) this.getFlowId()))) + this.getFlowletId()));
    }

    /**
     * Create a hash value which is dependent on the source, destination,
     * and flowlet identifier of this packet.
     *
     * This means the following:
     * (a) The sequential hash: starting at non-sequential hash base based on (src, dst), it linearly adds
     *     the respective flowlet identifier.
     *
     * (b) The non-sequential hash: complete hash dependent on (src, dst, flowletId).
     */
    default void setHashSrcDstFlowletDependent() {
        setNonSequentialHash(absolute(hash(this.getSourceId() + hash(this.getDestinationId())) + this.getFlowletId()));
    }

    /**
     * Uses Robert Jenkins' 32 bit integer hash function
     * Source: http://burtleburtle.net/bob/hash/integer.html
     *
     * Thomas Wang, Jan 1997
     * last update Mar 2007
     * Version 3.1
     *
     * @param a   Input value
     *
     * @return Hash value
     */
    static int hash(int a) {
        a = (a+0x7ed55d16) + (a<<12);
        a = (a^0xc761c23c) ^ (a>>19);
        a = (a+0x165667b1) + (a<<5);
        a = (a+0xd3a2646c) ^ (a<<9);
        a = (a+0xfd7046c5) + (a<<3);
        a = (a^0xb55a4f09) ^ (a>>16);
        a = absolute(a);
        return a;
    }

    /**
     * Forcefully convert integer to absolute.
     * Takes care of the special case when the integer is the minimum integer,
     * which Math.abs() cannot convert to an absolute value and returns a negative value.
     *
     * @param x     Integer value
     *
     * @return |x|
     */
    static int absolute(int x) {
        if (x == Integer.MIN_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return Math.abs(x);
        }
    }

}
