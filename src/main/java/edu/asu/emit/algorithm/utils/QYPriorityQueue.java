/*
 *
 * Copyright (c) 2004-2009 Arizona State University.  All rights
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
package edu.asu.emit.algorithm.utils;

import java.util.LinkedList;
import java.util.List;

import edu.asu.emit.algorithm.graph.BaseElementWithWeight;

/**
 * The class defines a priority queue. 
 * @author yqi
 * @date Mar 16, 2015
 * @param <E> the type of the element in the queue
 */
public class QYPriorityQueue<E extends BaseElementWithWeight> {
	private List<E> elementWeightPairList = new LinkedList<E>();
	private int limitSize = -1;
	private boolean isIncremental = false; 
	
	/**
	 * Default constructor. 
	 */
	public QYPriorityQueue() { }
	
	/**
	 * Constructor. 
	 * @param pLimitSize
	 */
	public QYPriorityQueue(int pLimitSize, boolean pIsIncremental) {
		limitSize = pLimitSize;
		isIncremental = pIsIncremental;
	}
		
	@Override
	public String toString() {
		return elementWeightPairList.toString();
	}
	
	/**
	 * Binary search is exploited to find the right position 
	 * of the new element. 
	 * @param weight
	 * @return the position of the new element
	 */
	private int binLocatePos(double weight, boolean isIncremental)	{
		int mid = 0;
		int low = 0;
		int high = elementWeightPairList.size() - 1;
		//
		while (low <= high) {
			mid = (low + high) / 2;
			if (elementWeightPairList.get(mid).getWeight() == weight) {
				return mid + 1;
			}
							
			if (isIncremental) {
				if (elementWeightPairList.get(mid).getWeight() < weight) {
					high = mid - 1;
				} else {
					low = mid + 1;
				}	
			} else {
				if (elementWeightPairList.get(mid).getWeight() > weight) {
					high = mid - 1;
				} else {
					low = mid + 1;
				}
			}	
		}
		return low;
	}
	
	/**
	 * Add a new element in the queue. 
	 * @param element
	 */
	public void add(E element) {
		elementWeightPairList.add(binLocatePos(element.getWeight(), isIncremental), element);
		
		if (limitSize > 0 && elementWeightPairList.size() > limitSize) {
			int sizeOfResults = elementWeightPairList.size();
			elementWeightPairList.remove(sizeOfResults - 1);			
		}
	}
	
	/**
	 * It only reflects the size of the current results.
	 * @return
	 */
	public int size() {
		return elementWeightPairList.size();
	}
	
	/**
	 * Get the i th element. 
	 * @param i
	 * @return
	 */
	public E get(int i) {
		if (i >= elementWeightPairList.size()) {
			System.err.println("The result :" + i + " doesn't exist!!!");
		}
		return elementWeightPairList.get(i);
	}
	
	/**
	 * Get the first element, and then remove it from the queue. 
	 * @return
	 */
	public E poll() {
		E ret = elementWeightPairList.get(0);
		elementWeightPairList.remove(0);
		return ret;
	}
	
	/**
	 * Check if it's empty.
	 * @return
	 */
	public boolean isEmpty() {
		return elementWeightPairList.isEmpty();
	}
	
}
