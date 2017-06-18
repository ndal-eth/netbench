/*
 *
 * Copyright (c) 2004-2008 Arizona State University.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ARIZONA STATE UNIVERSITY ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL ARIZONA STATE UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package edu.asu.emit.algorithm.graph;


import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * The class defines a directed graph.
 * 
 * @author yqi
 * @author snkas
 */
public class Graph implements BaseGraph {

    public static final long DISCONNECTED = 1000000000;

    // Maps a vertex identifier to all vertices a directed edges exists from itself to them
    private final Map<Integer, List<Vertex>> outEdges;

    // Maps a vertex identifier to all vertices a directed edges exists to itself from them
    private final Map<Integer, List<Vertex>> inEdges;

    // Maps a pair of vertex identifiers (an edge) to its weight
    private final Map<Pair<Integer, Integer>, Long> edgeWeights;

    // Maps a vertex identifier to its vertex object
    private final Map<Integer, Vertex> idVertexIndex;

    // List of all vertices in the graph
    private final List<Vertex> vertexList;

    /**
     * Constructor to create a graph of a certain size with the given edges.
     *
     * @param n                     Number of nodes
     * @param linkDirectedPairs     List of directed pairs representing edges
     */
    public Graph(int n, List<Pair<Integer, Integer>> linkDirectedPairs) {

        // Initialize default data structures
        this.idVertexIndex = new HashMap<>();
        this.vertexList = new Vector<>();
        this.edgeWeights = new HashMap<>();
        this.inEdges = new HashMap<>();
        this.outEdges = new HashMap<>();

        // Add vertex presence to data structures
        for (int id = 0; id < n; id++) {
            Vertex vertex = new Vertex(id);
            vertexList.add(vertex);
            idVertexIndex.put(id, vertex);
            inEdges.put(id, new ArrayList<>());
            outEdges.put(id, new ArrayList<>());
        }

        // Add edge presence to data structures
        for (Pair<Integer, Integer> linkDirPair : linkDirectedPairs) {
            addEdge(linkDirPair.getLeft(), linkDirPair.getRight(), 1);
        }

    }

    /**
     * Constructor to create a shallow copy of another graph.
     *
     * @param graph Graph instance
     */
	Graph(Graph graph) {

        // Default instantiate data structures
        this.idVertexIndex = new HashMap<>();
        this.vertexList = new Vector<>();
        this.edgeWeights = new HashMap<>();
        this.inEdges = new HashMap<>();
        this.outEdges = new HashMap<>();

        // Shallow copy other graph
        this.vertexList.addAll(graph.vertexList);
        this.idVertexIndex.putAll(graph.idVertexIndex);
        this.inEdges.putAll(graph.inEdges);
        this.outEdges.putAll(graph.outEdges);
        this.edgeWeights.putAll(graph.edgeWeights);

	}

	/**
	 * Add edge (start, end): weight to the graph.
	 *
	 * @param startVertexId     Start vertex identifier of the edge
	 * @param endVertexId       End vertex identifier of the edge
	 * @param weight            Edge weight
	 */
	private void addEdge(int startVertexId, int endVertexId, long weight) {

        // Check that the vertex identifiers exist
        if (!idVertexIndex.containsKey(startVertexId) || !idVertexIndex.containsKey(endVertexId) || startVertexId == endVertexId) {
            throw new IllegalArgumentException("Graph: addEdge: the edge (" + startVertexId + ", " + endVertexId + ") does not exist in the graph.");
        }

        // Check that the edge does not already exist
        if (edgeWeights.containsKey(new ImmutablePair<>(startVertexId, endVertexId))) {
            throw new IllegalArgumentException("Graph: addEdge: the edge (" + startVertexId + ", " + endVertexId + ") already exists.");
        }

        // Add to inward and outward edge list
        outEdges.get(startVertexId).add(idVertexIndex.get(endVertexId));
        inEdges.get(endVertexId).add(idVertexIndex.get(startVertexId));

        // Store into edge weight map
        edgeWeights.put(new ImmutablePair<>(startVertexId, endVertexId), weight);

    }

    /**
     * Retrieve all vertices that have an edge incoming from the given vertex.
     *
     * @param vertex    Vertex instance
     *
     * @return  List of vertices that have edge incoming from the vertex
     */
    @Override
    public List<Vertex> getAdjacentVertices(Vertex vertex) {
        return outEdges.containsKey(vertex.getId()) ? outEdges.get(vertex.getId()) : new ArrayList<>();
    }

    /**
     * Retrieve all vertices that have an edge toward the given vertex.
     *
     * @param vertex    Vertex instance
     *
     * @return  List of vertices that have edge towards it
     */
    @Override
    public List<Vertex> getPrecedentVertices(Vertex vertex) {
        return inEdges.containsKey(vertex.getId()) ? inEdges.get(vertex.getId()) : new ArrayList<>();
    }

    /**
     * Retrieve the weight of directed edge (src, dst).
     *
     * @param source    Source vertex
     * @param target    Destination vertex
     *
     * @return  Edge weight
     */
    @Override
    public long getEdgeWeight(Vertex source, Vertex target) {
        if (edgeWeights.containsKey(new ImmutablePair<>(source.getId(), target.getId()))) {
            return edgeWeights.get(new ImmutablePair<>(source.getId(), target.getId()));
        } else {
            throw new RuntimeException("Graph: getEdgeWeight: cannot retrieve edge weight of non-existing edge.");
        }
    }

    /**
     * Return the vertex list in the graph.
     *
     * @return  List of all vertices in the graph
     */
    @Override
    public List<Vertex> getVertexList() {
        return vertexList;
    }

    /**
     * Get the vertex with the corresponding identifier.
     *
     * @param id    Vertex identifier
     *
     * @return  Vertex instance (if not found, throws RuntimeException)
     */
    public Vertex getVertex(int id) {
        if (idVertexIndex.containsKey(id)) {
            return idVertexIndex.get(id);
        } else {
            throw new RuntimeException("Graph: getVertex: cannot retrieve vertex for invalid identifier.");
        }
    }
    
}
