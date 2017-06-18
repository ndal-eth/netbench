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

import java.util.List;
import java.util.Vector;

/**
 * The class defines a path in graph.
 * 
 * @author yqi
 * @author snkas
 */
public class Path implements BaseElementWithWeight {

	// List of vertices in the path
	private final List<Vertex> vertexList;

    // Total path weight
	private long weight;

    /**
     * Create a path which should have a pre-determined weight.
     *
     * Afterwards, the vertex list *must* be adapted to include
     * all vertices on the path such that the weight is summed up correctly
     * to the total weight.
     *
     * @param weight    Total path weight
     */
	public Path(long weight) {
        this.vertexList = new Vector<>();
        this.weight = weight;
    }

    /**
     * Create a path with a pre-determined vertex list and weight.
     *
     * @param vertexList    Path vertex list
     * @param weight        Total path weight
     */
	public Path(List<Vertex> vertexList, long weight) {
		this.vertexList = vertexList;
		this.weight = weight;
	}

    /**
     * Retrieve the total weight of the path.
     *
     * @return Total path weight
     */
	public long getWeight() {
		return weight;
	}

    /**
     * Set the total weight of the path.
     *
     * @param weight    Total path weight
     */
    public void setWeight(long weight) {
        this.weight = weight;
    }

    /**
     * Retrieve list of all vertices on the path (source to destination).
     *
     * @return  Path vertex list
     */
	public List<Vertex> getVertexList() {
		return vertexList;
	}
	
	@Override
	public boolean equals(Object right) {
		
		if (right instanceof Path) {
			Path rPath = (Path) right;
			return vertexList.equals(rPath.vertexList);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return vertexList.hashCode();
	}

    @Override
	public String toString() {
		return vertexList.toString() + ":" + weight;
	}

}
