package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.HashTable;

public class HashTableImpl<Key, Value> implements HashTable<Key,Value> {
	//all checks on array size depend on backingArray.size or this variable. Can dynamically resize by just changing this
	private int arraySize = 5;
	private double loadFactor = 0.75;
	private int numberOfEntries = 0;
	//Arrays can be *declared* with Generic/Parameterized types but cannot be *instantiated* as such 
	private HashNode<Key,Value>[] backingArray;
	@SuppressWarnings("unchecked")
	public HashTableImpl() {
		backingArray = new HashNode[arraySize];
	}
	
	public Value put(Key k, Value v) {
		//put(K, null) should call remove(K) 
		if (v == null) {
			return remove(k);
		}
		//store bucketIndex on hash variable instead of constant method calls
		int hash = (k.hashCode() & 0x7FFFFFFF) % arraySize;
		HashNode<Key,Value> head = backingArray[hash];
		//check if Key is already in HashTable by iterating through backing linkedList
		while (head!=null) {
			if (head.hash==k.hashCode() && head.key.equals(k)) {
				Value returnValue = head.value;
				head.value = v;				
				return returnValue;
			}
			head = head.next;
		}
		//check if the number of entries is greater than or equal to the size needed to trigger an increase
		if (numberOfEntries >= (backingArray.length * loadFactor)) {
			resize(backingArray.length*2);
			//if table is resized the hash must be recalculated 
			hash = (k.hashCode() & 0x7FFFFFFF) % arraySize;
		}
		//if Key is not already in table place it in the front by putting it in the proper place in the array and linking previous head in next value
		//iterating to the back of the list and placing it there would take O(n) instead of O(1) and would be less clean
		HashNode<Key, Value> next = backingArray[hash];
		backingArray[hash] = new HashNode<Key, Value>(k.hashCode(), k, v, next);
		numberOfEntries++;
		return null;
	}
	
	//iterates through linkedList to find Key, if can't find return null
	public Value get(Key k) {
		if (k == null) {
			throw new IllegalArgumentException();
		}
		int hash = (k.hashCode() & 0x7FFFFFFF) % backingArray.length;
		HashNode<Key,Value> head = backingArray[hash];
		while (head!=null) {
			if (head.hash==k.hashCode() && head.key.equals(k)) {
				return head.value;
			}
			head = head.next;
		}
		return null;
	}
	
	private Value remove(Key k) {
		if (k == null) {
			throw new IllegalArgumentException();
		}
		int hash = (k.hashCode() & 0x7FFFFFFF) % backingArray.length;
		HashNode<Key,Value> curr = (HashNode<Key,Value>)backingArray[hash];
		HashNode<Key,Value> prev = null;
		while (curr!=null) {
			if (curr.hash==k.hashCode() && curr.key.equals(k)) {
				//store return value to be returned after node is dropped
				Value returnValue = curr.value;
				//when curr finds the Key, drop the node by stitching prev.next to curr.next
				if (prev != null) {
					prev.next = curr.next;
				//edge case if prev == null then Key is the first value in the array in which case we drop it by placing prev direct in the array
				//although strictly speaking, iterating through the linked list should be the edge case
				} else {
					backingArray[hash] = curr.next;
				}
				numberOfEntries--;
				return returnValue;
			}
			//iterate both values once
			prev = curr;
			curr = curr.next;
		}
		return null;
	}
	
	private void resize(int newSize) {
		@SuppressWarnings("unchecked")
		HashNode<Key,Value>[] newBackingArray = new HashNode[newSize];
		
		for (int i = 0; i < backingArray.length; i++) {
			HashNode<Key,Value> head = backingArray[i];
			while(head!=null) {
				//need to declare and iterate head because once we place it in the new array the .next value will be changed
				//to be precise the linked list will be placed in backwards because for each .next iteration the element will be place in the *front* of the array
				HashNode<Key,Value> element = head;
				head = head.next;
				int index = (element.hash & 0x7FFFFFFF) % newBackingArray.length;
				element.next = newBackingArray[index];
				newBackingArray[index] = element;
			}
		}
		backingArray = newBackingArray;
		arraySize = backingArray.length;
	}
	
	@SuppressWarnings("hiding")
	private class HashNode<Key, Value> {
		//instantiate hash int on Object to obviate need for constant method calls
		final private int hash;
		final private Key key;
		//both value and next fields can be changed; no reason to ever change the Key or Hash fields
		private Value value;
		private HashNode<Key, Value> next;
		private HashNode(int hash, Key key, Value value, HashNode<Key, Value> next) {
			this.hash = hash;
			this.key = key;
			this.value = value;
			this.next = next;
		}
	}
}