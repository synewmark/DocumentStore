package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {
	private final int startingSize = 5;
	
	@SuppressWarnings("unchecked")
	public MinHeapImpl() {
		elements = (E[]) new Comparable[startingSize];
	}

	@Override
	public void reHeapify(E element) {
		int position = getArrayIndex(element);
		downHeap(position);
		upHeap(position);
	}

	@Override
	protected int getArrayIndex(E element) {
		for (int i = 1; i <= count; i++) {
			if (elements[i].equals(element)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	protected void doubleArraySize() {
		@SuppressWarnings("unchecked")
		E[] newElements = (E[]) new Comparable[elements.length*2];
		//could also do System.arraycopy(elements, 1, newElements, 1, count) to save a copy but this looks cleaner
		System.arraycopy(elements, 0, newElements, 0, elements.length);
		elements = newElements;
	}
	
	@Override
	public void insert(E x) {
        // double size of array if necessary
        if (this.count >= this.elements.length - 1) {
            this.doubleArraySize();
        }
        //check if value is already in heap
        int curr = getArrayIndex(x);
        if (curr != -1) {
        	//if value is already in heap, reheapify
        	this.upHeap(curr);
        	this.downHeap(curr);
        } else {
	        //add x to the bottom of the heap
	        this.elements[++this.count] = x;
	        //percolate it up to maintain heap order property
	        this.upHeap(this.count);
        }
	}
}
