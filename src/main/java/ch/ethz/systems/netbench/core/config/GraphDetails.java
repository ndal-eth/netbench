package ch.ethz.systems.netbench.core.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GraphDetails {

    private final String idHash;
    private int numNodes;
    private int numEdges;
    private Set<Integer> serverNodeIds;
    private Set<Integer> switchNodeIds;
    private Set<Integer> torNodeIds;
    private HashMap<Integer, Set<Integer>> torToServerIds;
    private HashMap<Integer, Integer> serverToTorId;
    private boolean autoExtended;

    GraphDetails(String idHash) {
        this.idHash = idHash;
        this.numNodes = -1;
        this.numEdges = -1;
        this.serverNodeIds = null;
        this.switchNodeIds = null;
        this.torNodeIds = null;
        this.autoExtended = false;
    }

    /**
     * Get the identifying hash for the this topology.
     * The identifying hash is the SHA-1 hash of the topology file's content.
     *
     * @return  Topology identifying hash
     */
    public String getIdHash() {
        return idHash;
    }

    /**
     * Set the nodes having a transport layer using a set of identifiers.
     *
     * @param torNodeIds   Set of identifiers
     */
    void setServerNodeIds(Set<Integer> torNodeIds) {
        this.serverNodeIds = torNodeIds;
    }

    /**
     * Set the nodes which only function as switches.
     *
     * @param switchNodeIds   Set of identifiers
     */
    void setSwitchNodeIds(Set<Integer> switchNodeIds) {
        this.switchNodeIds = switchNodeIds;
    }

    /**
     * Set the nodes marked as ToRs using a set of identifiers.
     *
     * @param torNodeIds   Set of identifiers
     */
    void setTorNodeIds(Set<Integer> torNodeIds) {
        this.torNodeIds = torNodeIds;
        this.torToServerIds = new HashMap<>();
        for (Integer x : torNodeIds) {
            torToServerIds.put(x, new HashSet<Integer>());
        }
        this.serverToTorId = new HashMap<>();
    }

    /**
     * Save that a ToR has a server linked to it.
     *
     * @param torId     ToR identifier
     * @param serverId  Server identifier
     */
    void saveTorHasServer(int torId, int serverId) {
        torToServerIds.get(torId).add(serverId);
        serverToTorId.put(serverId, torId);
    }

    /**
     * Set the number of nodes.
     *
     * @param numNodes  Number of nodes
     */
    void setNumNodes(int numNodes) {
        assert(numNodes >= 0);
        this.numNodes = numNodes;
    }

    /**
     * Set the number of edges.
     *
     * @param numEdges  Number of edges
     */
    void setNumEdges(int numEdges) {
        assert(numEdges >= 0);
        this.numEdges = numEdges;
    }

    /**
     * Set that the graph was automatically auto-extended.
     */
    void setIsAutoExtended() {
        this.autoExtended = true;
    }

    /**
     * Retrieve number of nodes.
     *
     * @return  Number of nodes
     */
    public int getNumNodes() {
        return numNodes;
    }

    /**
     * Retrieve number of edges.
     *
     * @return  Number of edges
     */
    public int getNumEdges() {
        return numEdges;
    }

    /**
     * Retrieve all node identifiers marked with having a transport layer.
     *
     * @return  List of transport layer node identifiers
     */
    public Set<Integer> getServerNodeIds() {
        return Collections.unmodifiableSet(serverNodeIds);
    }

    /**
     * Retrieve all node identifiers only doing switching.
     *
     * @return  List of switch node identifiers
     */
    public Set<Integer> getSwitchNodeIds() {
        return Collections.unmodifiableSet(switchNodeIds);
    }

    /**
     * Retrieve all node identifiers marked as ToR.
     *
     * @return  List of ToR node identifiers
     */
    public Set<Integer> getTorNodeIds() {
        return Collections.unmodifiableSet(torNodeIds);
    }

    /**
     * Get all the servers associated with a certain ToR.
     *
     * @param torNodeId     ToR node identifier
     *
     * @return Set of associated servers
     */
    public Set<Integer> getServersOfTor(int torNodeId) {
        return Collections.unmodifiableSet(torToServerIds.get(torNodeId));
    }

    /**
     * Retrieve the ToR identifier of ToR to which the server belongs and is connected to.
     *
     * @param serverId  Server identifier
     *
     * @return  ToR identifier
     */
    public Integer getTorIdOfServer(int serverId) {
        return serverToTorId.get(serverId);
    }

    /**
     * Check whether the graph was extended.
     *
     * @return  True iff the graph was extended with servers
     */
    public boolean isAutoExtended() {
        return autoExtended;
    }

    /**
     * Get the number of Top of Racks (ToRs) (either all
     * ToRs are servers, or every server is connected to a ToR).
     *
     * @return  Number of ToRs
     */
    public int getNumTors() {
        return torNodeIds.size();
    }

    /**
     * Get the number of switches (nodes that cannot have
     * a transport layer, nor be directly connected to a server).
     *
     * @return  Number of switches
     */
    public int getNumSwitches() {
        return switchNodeIds.size();
    }

    /**
     * Get the number of servers (nodes with transport layer).
     *
     * @return  Number of servers
     */
    public int getNumServers() {
        return serverNodeIds.size();
    }

}
