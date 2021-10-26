package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T>{
	int size;
	StackElement<T> head;
	public StackImpl() {
		this.head = null;
		this.size = 0;
	}
	
	public void push(T element) {
		if (element==null) {
			throw new IllegalArgumentException();
		}
		head = new StackElement<T>(element, head);
		size++;
	}
	
	public T pop() {
		if (head == null) {
			return null;
		}
		T returnValue = head.t;
		head = head.next;
		size--;
		return returnValue;
	}
	
	public T peek() {
		if (head==null) {
			return null;
		}
		return head.t;
	}
	
	public int size() {
		return size;
	}
//	public void printEverything() {
//		StackElement<T> curr = head;
//		while(curr!=null) {
//			System.out.print(curr.t+" ");
//			System.out.println();
//			curr = curr.next;
//		}
//	}
	@SuppressWarnings("hiding")
	private class StackElement<T> {
		private final T t;
		private StackElement<T> next;
		private StackElement(T t, StackElement<T> next) {
			this.t = t;
			this.next = next;
		}
	}
}