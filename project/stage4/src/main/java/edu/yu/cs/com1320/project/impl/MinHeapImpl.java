package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {
	private final int startingSize = 5;
	
	@SuppressWarnings("unchecked")
	public MinHeapImpl() {
		elements = (E[]) new Comparable[startingSize];
	}

	@Override
	public void reHeapify(E element) {
		//getArrayIndex throws NoSuchElementException for us if element is not in heap
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
		throw new NoSuchElementException(element.toString());
	}

	@Override
	protected void doubleArraySize() {
		@SuppressWarnings("unchecked")
		E[] newElements = (E[]) new Comparable[elements.length*2];
		//could also do System.arraycopy(elements, 1, newElements, 1, count) to save a copy but this looks cleaner
		System.arraycopy(elements, 0, newElements, 0, elements.length);
		elements = newElements;
	}

}
