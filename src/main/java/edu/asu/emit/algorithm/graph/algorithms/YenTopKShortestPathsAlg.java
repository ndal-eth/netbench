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
import edu.asu.emit.algorithm.graph.VariableGraph;
import edu.asu.emit.algorithm.graph.BaseGraph;
import edu.asu.emit.algorithm.graph.Vertex;
import edu.asu.emit.algorithm.utils.QYPriorityQueue;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision: 783 $
 * @latest $Id: YenTopKShortestPathsAlg.java 783 2009-06-19 19:19:27Z qyan $
 * @author snkas
 */
public class YenTopKShortestPathsAlg
{
	private VariableGraph graph = null;

	// intermediate variables
	private List<Path> resultList = new Vector<Path>();
	private Map<Path, Vertex> pathDerivationVertexIndex = new HashMap<Path, Vertex>();
	private QYPriorityQueue<Path> pathCandidates = new QYPriorityQueue<Path>();
	
	// the ending vertices of the paths
	private Vertex sourceVertex = null;
	private Vertex targetVertex = null;
	
	// variables for debugging and testing
	private int generatedPathNum = 0;
	
	/**
	 * Default constructor.
	 * 
	 * @param graph
	 */
	public YenTopKShortestPathsAlg(BaseGraph graph)	{
        this(graph, null, null);
	}
	
	/**
	 * Constructor 2
	 * 
	 * @param graph
	 * @param sourceVertex
	 * @param targetVertex
	 */
	public YenTopKShortestPathsAlg(BaseGraph graph, 
			Vertex sourceVertex, Vertex targetVertex)	{
		if (graph == null) {
			throw new IllegalArgumentException("A NULL graph object occurs!");
		}
		this.graph = new VariableGraph((Graph)graph);
		this.sourceVertex = sourceVertex;
		this.targetVertex = targetVertex;
		init();
	}
	
	/**
	 * Initiate members in the class. 
	 */
	private void init()	{
		clear();
		// get the shortest path by default if both source and target exist
		if (sourceVertex != null && targetVertex != null) {
			Path shortestPath = getShortestPath(sourceVertex, targetVertex);
			if (!shortestPath.getVertexList().isEmpty()) {
				pathCandidates.add(shortestPath);
				pathDerivationVertexIndex.put(shortestPath, sourceVertex);
			}
		}
	}
	
	/**
	 * Clear the variables of the class. 
	 */
	public void clear()	{
		pathCandidates = new QYPriorityQueue<Path>();
		pathDerivationVertexIndex.clear();
		resultList.clear();
		generatedPathNum = 0;
	}
	
	/**
	 * Obtain the shortest path connecting the source and the target, by using the
	 * classical Dijkstra shortest path algorithm. 
	 * 
	 * @param sourceVertex
	 * @param targetVertex
	 * @return
	 */
	public Path getShortestPath(Vertex sourceVertex, Vertex targetVertex)	{
		DijkstraShortestPathAlg dijkstraAlg = new DijkstraShortestPathAlg(graph);
		return dijkstraAlg.getShortestPath(sourceVertex, targetVertex);
	}
	
	/**
	 * Check if there exists a path, which is the shortest among all candidates.  
	 * 
	 * @return
	 */
	public boolean hasNext() {
        return !pathCandidates.isEmpty();
	}
	
	/**
	 * Get the shortest path among all that connecting source with targe. 
	 * 
	 * @return
	 */
	public Path next() {
		//3.1 prepare for removing vertices and arcs
		Path curPath = pathCandidates.poll();
		resultList.add(curPath);

		Vertex curDerivation = pathDerivationVertexIndex.get(curPath);
		int curPathHash =
			curPath.getVertexList().subList(0, curPath.getVertexList().indexOf(curDerivation)).hashCode();
		
		int count = resultList.size();
		
		//3.2 remove the vertices and arcs in the graph
		for (int i = 0; i < count-1; ++i) {
			Path curResultPath = resultList.get(i);
							
			int curDevVertexId =
				curResultPath.getVertexList().indexOf(curDerivation);
			
			if (curDevVertexId < 0) {
                continue;
            }

			// Note that the following condition makes sure all candidates should be considered. 
			/// The algorithm in the paper is not correct for removing some candidates by mistake. 
			int pathHash = curResultPath.getVertexList().subList(0, curDevVertexId).hashCode();
			if (pathHash != curPathHash) {
                continue;
            }
			
			Vertex curSuccVertex =
				curResultPath.getVertexList().get(curDevVertexId + 1);
			
			graph.deleteEdge(new ImmutablePair<Integer, Integer>(
                    curDerivation.getId(), curSuccVertex.getId()));
		}
		
		int pathLength = curPath.getVertexList().size();
		List<Vertex> curPathVertexList = curPath.getVertexList();
		for (int i = 0; i < pathLength-1; ++i) {
			graph.deleteVertex(curPathVertexList.get(i).getId());
			graph.deleteEdge(new ImmutablePair<Integer, Integer>(
                    curPathVertexList.get(i).getId(),
                    curPathVertexList.get(i + 1).getId()));
		}
		
		//3.3 calculate the shortest tree rooted at target vertex in the graph
		DijkstraShortestPathAlg reverseTree = new DijkstraShortestPathAlg(graph);
		reverseTree.getShortestPathFlower(targetVertex);
		
		//3.4 recover the deleted vertices and update the cost and identify the new candidate results
		boolean isDone = false;
		for (int i=pathLength-2; i>=0 && !isDone; --i)	{
			//3.4.1 get the vertex to be recovered
			Vertex curRecoverVertex = curPathVertexList.get(i);
			graph.recoverDeletedVertex(curRecoverVertex.getId());
			
			//3.4.2 check if we should stop continuing in the next iteration
			if (curRecoverVertex.getId() == curDerivation.getId()) {
				isDone = true;
			}
			
			//3.4.3 calculate cost using forward star form
			Path subPath = reverseTree.updateCostForward(curRecoverVertex);
			
			//3.4.4 get one candidate result if possible
			if (subPath != null) {
				++generatedPathNum;
				
				//3.4.4.1 get the prefix from the concerned path
				long cost = 0;
				List<Vertex> prePathList = new Vector<Vertex>();
				reverseTree.correctCostBackward(curRecoverVertex);
				
				for (int j=0; j<pathLength; ++j) {
					Vertex curVertex = curPathVertexList.get(j);
					if (curVertex.getId() == curRecoverVertex.getId()) {
						j=pathLength;
					} else {
						cost += graph.getEdgeWeightOfGraph(curPathVertexList.get(j),
								curPathVertexList.get(j+1));
						prePathList.add(curVertex);
					}
				}
				prePathList.addAll(subPath.getVertexList());

				//3.4.4.2 compose a candidate
				subPath.setWeight(cost + subPath.getWeight());
				subPath.getVertexList().clear();
				subPath.getVertexList().addAll(prePathList);
				
				//3.4.4.3 put it in the candidate pool if new
				if (!pathDerivationVertexIndex.containsKey(subPath)) {
					pathCandidates.add(subPath);
					pathDerivationVertexIndex.put(subPath, curRecoverVertex);
				}
			}
			
			//3.4.5 restore the edge
			Vertex succVertex = curPathVertexList.get(i + 1);
			graph.recoverDeletedEdge(new ImmutablePair<Integer, Integer>(
                    curRecoverVertex.getId(), succVertex.getId()));
			
			//3.4.6 update cost if necessary
            long cost1 = graph.getEdgeWeight(curRecoverVertex, succVertex)
				+ reverseTree.getStartVertexDistanceIndex().get(succVertex);
			
			if (reverseTree.getStartVertexDistanceIndex().get(curRecoverVertex) >  cost1) {
				reverseTree.getStartVertexDistanceIndex().put(curRecoverVertex, cost1);
				reverseTree.getPredecessorIndex().put(curRecoverVertex, succVertex);
				reverseTree.correctCostBackward(curRecoverVertex);
			}
		}
		
		//3.5 restore everything
		graph.recoverDeletedEdges();
		graph.recoverDeletedVertices();
		
		return curPath;
	}
	
	/**
	 * Get the top-K shortest paths connecting the source and the target.  
	 * This is a batch execution of top-K results.
	 * 
	 * @param source
	 * @param target
	 * @param k
	 * @return
	 */
	public List<Path> getShortestPaths(Vertex source,
                                       Vertex target, int k) {
		sourceVertex = source;
		targetVertex = target;
		
		init();
		int count = 0;
		while (hasNext() && count < k) {
			next();
			++count;
		}
		
		return resultList;
	}
		
	/**
	 * Return the list of results generated on the whole.
	 * (Note that some of them are duplicates)
	 * @return
	 */
	public List<Path> getResultList() {
        return resultList;
	}

	/**
	 * The number of distinct candidates generated on the whole. 
	 * @return
	 */
	public int getCadidateSize() {
		return pathDerivationVertexIndex.size();
	}

	public int getGeneratedPathSize() {
		return generatedPathNum;
	}
}
