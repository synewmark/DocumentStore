package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.HashTable;

public class HashTableImpl<Key, Value> implements HashTable<Key,Value> {
	//all checks on array size depend on backingArray.size or this variable. Can dynamically resize with just this
	private int arraySize = 5;
	//Arrays can be *declared* with Generic/Parameterized types but cannot be *instantiated* as such 
	private HashNode<Key,Value>[] backingArray;
	@SuppressWarnings("unchecked")
	public HashTableImpl() {
		backingArray = new HashNode[arraySize];
	}
	//put(K, null) should call remove(K) 
	public Value put(Key k, Value v) {
		if (v == null) {
			return remove(k);
		}
		//store bucketIndex on hash variable instead of constant method calls
		int hash = bucketIndex(k);
		HashNode<Key,Value> head = (HashNode<Key, Value>)backingArray[hash];
		//check if Key is already in HashTable by iterating through backing linkedList
		while (head!=null) {
			if (head.hash==k.hashCode() && head.key.equals(k)) {
				Value returnValue = head.value;
				head.value = v;				
				return returnValue;
			}
			head = head.next;
		}
		//if Key is not already in table place it in the front by putting it in the proper place in the array and linking previous head in next value
		//iterating to the back of the list and placing it there would take O(n) instead of O(1) and would be less clean
		HashNode<Key, Value> next = (HashNode<Key, Value>)backingArray[hash];
		backingArray[hash] = new HashNode<Key, Value>(k.hashCode(), k, v, next);
		return null;
	}
	//iterates through linkedList to find Key, if can't find return null
	public Value get(Key k) {
		int hash = bucketIndex(k);
		HashNode<Key,Value> head = (HashNode<Key,Value>)backingArray[hash];
		while (head!=null) {
			if (head.hash==k.hashCode() && head.key.equals(k)) {
				return head.value;
			}
			head = head.next;
		}
		return null;
	}
	private Value remove(Key k) {
		int hash = bucketIndex(k);
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
				//although strictly speaking, iterating through the linked list should be the edge case, whereas being first in the array is the ideal but my array is size 5
				} else {
					backingArray[hash] = curr.next;
				}
				return returnValue;
			}
			//iterate both values once
			prev = curr;
			curr = curr.next;
		}
		return null;
	}
	private int bucketIndex(Key k) {
		//both .hashCode() and % can return negative, Math.abs to keep it in array index format
		//return (k.hashCode() & 0x7FFFFFFF) % arraySize;
		return Math.abs(k.hashCode() % 5);
	}
	@SuppressWarnings("hiding")
	private class HashNode<Key, Value> {
		//instantiate hash int on Object to obviate need for constant method calls
		final private int hash;
		final private Key key;
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