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
 * The class defines a graph which can be changed constantly.
 * It is both capable to remove edges and vertices, and to recover those removed.
 *  
 * @author yqi
 * @author snkas
 */
public class VariableGraph extends Graph {

    // Set of identifiers of all removed vertices
	private Set<Integer> remVertexIdSet = new HashSet<>();

    // Set of all removed edges
	private Set<Pair<Integer, Integer>> remEdgeSet = new HashSet<>();

    /**
     * Constructor to create a graph from another graph.
     *
     * @param graph Graph instance
     */
	public VariableGraph(Graph graph) {
		super(graph);
	}
	
	/**
	 * Add an edge to the set of removed edges.
	 * 
	 * @param edge  Edge instance
	 */
	public void deleteEdge(Pair<Integer, Integer> edge) {
		remEdgeSet.add(edge);
	}
	
	/**
	 * Add a vertex to the set of removed vertices.
	 * 
	 * @param vertexId  Vertex identifier
	 */
	public void deleteVertex(Integer vertexId) {
		remVertexIdSet.add(vertexId);
	}

    /**
     * Recover ('un-delete') all deleted edges.
     */
	public void recoverDeletedEdges() {
		remEdgeSet.clear();
	}

    /**
     * Recover ('un-delete') a specific edge.
     *
     * @param edge  Directed edge pair
     */
	public void recoverDeletedEdge(Pair<Integer, Integer> edge)	{
		remEdgeSet.remove(edge);
	}

    /**
     * Recover ('un-delete') all deleted vertices.
     */
	public void recoverDeletedVertices() {
		remVertexIdSet.clear();
	}

    /**
     * Recover ('un-delete') a specific vertex.
     *
     * @param vertexId  Vertex identifier
     */
	public void recoverDeletedVertex(Integer vertexId) {
		remVertexIdSet.remove(vertexId);
	}

    /**
     * Retrieve the weight of directed edge (src, dst).
     * This excludes already 'deleted' vertices and edges.
     *
     * @param source    Source vertex
     * @param target    Destination vertex
     *
     * @return  Edge weight
     */
	@Override
	public long getEdgeWeight(Vertex source, Vertex target)	{

        // Check it is not deleted
		int sourceId = source.getId();
		int sinkId = target.getId();
		if (remVertexIdSet.contains(sourceId) || remVertexIdSet.contains(sinkId) || remEdgeSet.contains(new ImmutablePair<>(sourceId, sinkId))) {
            throw new RuntimeException("VariableGraph: getEdgeWeight: cannot access removed vertices or edges.");
		}

        // Ask parent to retrieve normally
		return super.getEdgeWeight(source, target);

	}

    /**
     * Retrieve the actual weight of directed edge (src, dst).
     *
     * @param source    Source vertex
     * @param target    Destination vertex
     *
     * @return  Edge weight
     */
	public long getEdgeWeightOfGraph(Vertex source, Vertex target) {
		return super.getEdgeWeight(source, target);
	}

    /**
     * Retrieve all vertices that have an edge incoming from the given vertex.
     * This excludes already 'deleted' vertices.
     *
     * @param vertex    Vertex instance
     *
     * @return  List of vertices that have edge incoming from the vertex
     */
    @Override
	public List<Vertex> getAdjacentVertices(Vertex vertex) {
        List<Vertex> retSet = new ArrayList<>();

        // Check that the vertex is not removed
        int startingVertexId = vertex.getId();
		if (!remVertexIdSet.contains(startingVertexId))	{

            // Go over all adjacent vertices (have edge to them)
			for (Vertex curVertex : super.getAdjacentVertices(vertex)) {

                // Check that the edge or its destination is not removed
				int endingVertexId = curVertex.getId();
				if (remVertexIdSet.contains(endingVertexId) || remEdgeSet.contains(new ImmutablePair<>(startingVertexId, endingVertexId))) {
					continue;
				}

				// Add to result set
				retSet.add(curVertex);

			}
		}

		return retSet;
	}

    /**
     * Retrieve all vertices that have an edge toward the given vertex.
     * This excludes already 'deleted' vertices.
     *
     * @param vertex    Vertex instance
     *
     * @return  List of vertices that have edge towards it
     */
    @Override
	public List<Vertex> getPrecedentVertices(Vertex vertex) {
        List<Vertex> retSet = new ArrayList<>();

        // Check that the vertex is not removed
        int endingVertexId = vertex.getId();
		if (!remVertexIdSet.contains(endingVertexId)) {

            // Go over all preceding vertices (have edge towards this vertex)
			for (Vertex curVertex : super.getPrecedentVertices(vertex)) {

                // Check that the edge or its origin is not removed
                int startingVertexId = curVertex.getId();
				if (remVertexIdSet.contains(startingVertexId) || remEdgeSet.contains(new ImmutablePair<>(startingVertexId, endingVertexId))) {
					continue;
				}

                // Add to result set
				retSet.add(curVertex);

			}
		}

		return retSet;
	}

    /**
     * Return the vertex list in the graph.
     * This excludes already 'deleted' vertices.
     *
     * @return  List of all vertices in the graph
     */
	public List<Vertex> getVertexList() {
		List<Vertex> retList = new Vector<>();
		for (Vertex curVertex : super.getVertexList()) {
			if (remVertexIdSet.contains(curVertex.getId())) {
				continue;
			}
			retList.add(curVertex);
		}
		return retList;
	}

    /**
     * Get the vertex with the corresponding identifier.
     *
     * @param id    Vertex identifier
     *
     * @return  Vertex instance (if not found, throws RuntimeException)
     */
	public Vertex getVertex(int id)	{
		if (remVertexIdSet.contains(id)) {
            throw new RuntimeException("VariableGraph: getVertex: cannot retrieve vertex for removed identifier.");
		} else {
			return super.getVertex(id);
		}
	}

}
