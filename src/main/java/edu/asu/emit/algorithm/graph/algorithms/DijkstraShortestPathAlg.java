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

package edu.asu.emit.algorithm.graph.algorithms;

import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.BaseGraph;
import edu.asu.emit.algorithm.graph.Vertex;

import java.util.*;


/**
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision: 430 $
 * @latest $Date: 2008-07-27 16:31:56 -0700 (Sun, 27 Jul 2008) $
 * @author snkas
 */
public class DijkstraShortestPathAlg
{
	// Input
	private final BaseGraph graph;

	// Intermediate variables
	private Set<Vertex> determinedVertexSet = new HashSet<Vertex>();
	private PriorityQueue<Vertex> vertexCandidateQueue = new PriorityQueue<Vertex>();
	private Map<Vertex, Long> startVertexDistanceIndex = new HashMap<Vertex, Long>();
	private Map<Vertex, Vertex> predecessorIndex = new HashMap<Vertex, Vertex>();

	/**
	 * Default constructor.
	 * @param graph
	 */
	public DijkstraShortestPathAlg(final BaseGraph graph) {
        this.graph = graph;
	}

	/**
	 * Clear intermediate variables.
	 */
	public void clear()	{
		determinedVertexSet.clear();
		vertexCandidateQueue.clear();
		startVertexDistanceIndex.clear();
		predecessorIndex.clear();
	}

	/**
	 * Getter for the distance in terms of the start vertex
	 * 
	 * @return
	 */
	public Map<Vertex, Long> getStartVertexDistanceIndex() {
        return startVertexDistanceIndex;
	}

	/**
	 * Getter for the index of the predecessors of vertices
	 * @return
	 */
	public Map<Vertex, Vertex> getPredecessorIndex() {
        return predecessorIndex;
	}

	/**
	 * Construct a tree rooted at "root" with 
	 * the shortest paths to the other vertices.
	 * 
	 * @param root
	 */
	public void getShortestPathTree(Vertex root) {
        determineShortestPaths(root, null, true);
	}
	
	/**
	 * Construct a flower rooted at "root" with 
	 * the shortest paths from the other vertices.
	 * 
	 * @param root
	 */
	public void getShortestPathFlower(Vertex root) {
        determineShortestPaths(null, root, false);
	}
	
	/**
	 * Do the work
	 */
	protected void determineShortestPaths(Vertex sourceVertex,
                                          Vertex sinkVertex, boolean isSource2sink)	{
		// 0. clean up variables
		clear();
		
		// 1. initialize members
		Vertex endVertex = isSource2sink ? sinkVertex : sourceVertex;
		Vertex startVertex = isSource2sink ? sourceVertex : sinkVertex;
		startVertexDistanceIndex.put(startVertex, 0l);
		startVertex.setWeight(0l);
		vertexCandidateQueue.add(startVertex);

		// 2. start searching for the shortest path
		while (!vertexCandidateQueue.isEmpty()) {
			Vertex curCandidate = vertexCandidateQueue.poll();

			if (curCandidate.equals(endVertex)) {
                break;
            }

			determinedVertexSet.add(curCandidate);

			updateVertex(curCandidate, isSource2sink);
		}
	}

	/**
	 * Update the distance from the source to the concerned vertex.
	 * @param vertex
	 */
	private void updateVertex(Vertex vertex, boolean isSource2sink)	{
		// 1. get the neighboring vertices 
        List<Vertex> neighborVertexList = isSource2sink ?
			graph.getAdjacentVertices(vertex) : graph.getPrecedentVertices(vertex);
			
		// 2. update the distance passing on current vertex
		for (Vertex curAdjacentVertex : neighborVertexList) {

			// 2.1 skip if visited before
			if (determinedVertexSet.contains(curAdjacentVertex)) {
                continue;
            }
			
			// 2.2 calculate the new distance
			long distance = startVertexDistanceIndex.containsKey(vertex)?
					startVertexDistanceIndex.get(vertex) : Graph.DISCONNECTED;
					
			distance += isSource2sink ? graph.getEdgeWeight(vertex, curAdjacentVertex)
					: graph.getEdgeWeight(curAdjacentVertex, vertex);

			// 2.3 update the distance if necessary
			if (!startVertexDistanceIndex.containsKey(curAdjacentVertex)
			|| startVertexDistanceIndex.get(curAdjacentVertex) > distance) {
				startVertexDistanceIndex.put(curAdjacentVertex, distance);

				predecessorIndex.put(curAdjacentVertex, vertex);
				
				curAdjacentVertex.setWeight(distance);
				vertexCandidateQueue.add(curAdjacentVertex);
			}
		}
	}
	
	/**
	 * Note that, the source should not be as same as the sink! (we could extend 
	 * this later on)
	 *  
	 * @param sourceVertex
	 * @param sinkVertex
	 * @return
	 */
	public Path getShortestPath(Vertex sourceVertex, Vertex sinkVertex)	{
		determineShortestPaths(sourceVertex, sinkVertex, true);
		//
		List<Vertex> vertexList = new Vector<Vertex>();
		long weight = startVertexDistanceIndex.containsKey(sinkVertex) ?
			startVertexDistanceIndex.get(sinkVertex) : Graph.DISCONNECTED;
		if (weight != Graph.DISCONNECTED) {
			Vertex curVertex = sinkVertex;
			do {
				vertexList.add(curVertex);
				curVertex = predecessorIndex.get(curVertex);
			} while (curVertex != null && curVertex != sourceVertex);
			vertexList.add(sourceVertex);
			Collections.reverse(vertexList);
		}
		return new Path(vertexList, weight);
	}

	/**
	 * Calculate the distance from the target vertex to the input 
	 * vertex using forward star form. 
	 * (FLOWER)
	 * 
	 * @param vertex
	 */
	public Path updateCostForward(Vertex vertex) {
		long cost = Graph.DISCONNECTED;

		// 1. get the set of successors of the input vertex
        List<Vertex> adjVertexSet = graph.getAdjacentVertices(vertex);
		
		// 2. make sure the input vertex exists in the index
		if (!startVertexDistanceIndex.containsKey(vertex)) {
			startVertexDistanceIndex.put(vertex, Graph.DISCONNECTED);
		}
		
		// 3. update the distance from the root to the input vertex if necessary
		for (Vertex curVertex : adjVertexSet) {
			// 3.1 get the distance from the root to one successor of the input vertex
			long distance = startVertexDistanceIndex.containsKey(curVertex)?
					startVertexDistanceIndex.get(curVertex) : Graph.DISCONNECTED;
					
			// 3.2 calculate the distance from the root to the input vertex
			distance += graph.getEdgeWeight(vertex, curVertex);
			//distance += ((VariableGraph)graph).get_edge_weight_of_graph(vertex, curVertex);
			
			// 3.3 update the distance if necessary 
			long costOfVertex = startVertexDistanceIndex.get(vertex);

			if(costOfVertex > distance)	{
				startVertexDistanceIndex.put(vertex, distance);
				predecessorIndex.put(vertex, curVertex);
				cost = distance;
			}
		}
		
		// 4. create the subPath if exists
		Path subPath = null;

		if (cost < Graph.DISCONNECTED) {
			subPath = new Path(cost);
			List<Vertex> vertexList = subPath.getVertexList();
			vertexList.add(vertex);
			
			Vertex selVertex = predecessorIndex.get(vertex);
			while (selVertex != null) {
				vertexList.add(selVertex);
				selVertex = predecessorIndex.get(selVertex);
			}
		}
		
		return subPath;
	}
	
	/**
	 * Correct costs of successors of the input vertex using backward star form.
	 * (FLOWER)
	 * 
	 * @param vertex
	 */
	public void correctCostBackward(Vertex vertex) {
		// 1. initialize the list of vertex to be updated
		List<Vertex> vertexList = new LinkedList<Vertex>();
		vertexList.add(vertex);
		
		// 2. update the cost of relevant precedents of the input vertex
		while (!vertexList.isEmpty()) {
			Vertex curVertex = vertexList.remove(0);
			long costOfCurVertex = startVertexDistanceIndex.get(curVertex);

            List<Vertex> preVertexSet = graph.getPrecedentVertices(curVertex);
			for (Vertex preVertex : preVertexSet) {
				long costOfPreVertex = startVertexDistanceIndex.containsKey(preVertex) ?
						startVertexDistanceIndex.get(preVertex) : Graph.DISCONNECTED;
						
				long freshCost = costOfCurVertex + graph.getEdgeWeight(preVertex, curVertex);
				if (costOfPreVertex > freshCost) {
					startVertexDistanceIndex.put(preVertex, freshCost);
					predecessorIndex.put(preVertex, curVertex);
					vertexList.add(preVertex);
				}
			}
		}
	}
	
}
