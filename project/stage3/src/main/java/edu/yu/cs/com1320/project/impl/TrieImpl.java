package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;


public class TrieImpl<Value> implements Trie<Value> {
	private final int alphabetSize = 36;
	private Node<Value> root;
	
	public TrieImpl() {
		root = new Node<Value>();
	}
	
    public void put(String key, Value val) {
    	if (key==null) {
    		throw new IllegalArgumentException();
    	}
    	if (key.length()==0) {
    		return;
    	}
    	if (val==null) {
    		return;
    	}
    	root = put(root, key, val, 0);
    }
    
	private Node<Value> put(Node<Value> x, String key, Value val, int d) {
		//make nodes along the way if they're missing
    	if (x == null) {
			x = new Node<Value>();
		}
		//if d == key.length we've reached our destination and can add val to the value set
		if (d == key.length()) {
			x.valueSet.add(val);
			return x;
		}
		char c = key.charAt(d);
		x.links[Character.getNumericValue(c)] = put(x.links[Character.getNumericValue(c)], key, val, d+1);
		return x;
    	
    }
    
    public List<Value> getAllSorted(String key, Comparator<Value> comparator) {
    	if (key == null||comparator == null) {
    		throw new IllegalArgumentException();
    	}
    	if (key.contains(" ")) {
    		throw new IllegalArgumentException();
    	}
    	if (key == "") {
    		return new ArrayList<Value>();
    	}
    	List <Value> returnList = getAllSorted(root, key, 0);
    	Collections.sort(returnList, comparator);
    	return returnList;
    }
    
    private List<Value> getAllSorted(Node<Value> x, String key, int d) {    	
    	if (d == key.length()) {
    		List<Value> returnList = new ArrayList<Value>();
    		returnList.addAll(x.valueSet);
        	return returnList;
    	}
    	char c = key.charAt(d);
    	return x.links[Character.getNumericValue(c)] == null ? new ArrayList<Value>() : getAllSorted(x.links[Character.getNumericValue(c)], key, d+1);
    }
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {
    	if (prefix == null||comparator == null) {
    		throw new IllegalArgumentException();
    	}
    	if (prefix.contains(" ")) {
    		throw new IllegalArgumentException();
    	}
    	if (prefix == "") {
    		return new ArrayList<Value>();
    	}
    	Set<Value> returnSet = new HashSet<>();
    	getAllWithPrefixSorted(root, prefix, 0, returnSet);
    	List<Value> returnList = new ArrayList<Value>(returnSet);
    	Collections.sort(returnList, comparator);
    	return returnList;
    }
    private void getAllWithPrefixSorted(Node<Value> x, String prefix, int d, Set<Value> set) {    	
    	if (d < prefix.length()) {
    		char c = prefix.charAt(d);
    		if (x.links[Character.getNumericValue(c)] != null) {
    			getAllWithPrefixSorted(x.links[Character.getNumericValue(c)], prefix, d+1, set);
    		}
    		return;
    	}
    	set.addAll(x.valueSet);
    	for (int i = 0; i < x.links.length; i++) {
    		if (x.links[i] != null) {
    			getAllWithPrefixSorted(x.links[i], prefix, d, set);
    		}
    	}
  
    }
    
    public Set<Value> deleteAll(String key) {
    	if (key == null) {
    		throw new IllegalArgumentException();
    	}
    	if (key.contains(" ")) {
    		throw new IllegalArgumentException();
    	}
    	if (key == "") {
    		return new HashSet<Value>();
    	}
    	Set<Value> returnSet = new HashSet<>();
    	deleteAll(root, key, 0, returnSet);
    	return returnSet;
    }
    
    private Node<Value> deleteAll(Node<Value> x, String key, int d, Set<Value> set) {
    	
    	if (x == null) {
    		return x;
    	}
    	
    	if (d < key.length()) {
    		char c = key.charAt(d);
    		x.links[Character.getNumericValue(c)] = deleteAll(x.links[Character.getNumericValue(c)], key, d+1, set);
    	}
    	
    	if (d == key.length()) {
    		set.addAll(x.valueSet);
        	x.valueSet.clear();
    	}
//need to determine return value to decide whether to prune
//if valueSet is not empty we know branch must be kept
    	if (!x.valueSet.isEmpty()) {
    		return x;
    	}
//if valueSet is not empty, must have live children or else will be pruned
    	for (int i = 0; i < x.links.length; i++) {
    		if (x.links[i] != null) {
    			return x;
    		}
    	}
    	return null;
    	
    }
    
    public Set<Value> deleteAllWithPrefix(String prefix) {
    	if (prefix == null) {
    		throw new IllegalArgumentException();
    	}
    	if (prefix.contains(" ")) {
    		throw new IllegalArgumentException();
    	}
    	if (prefix == "") {
    		return new HashSet<Value>();
    	}
    	Set<Value> returnSet = new HashSet<>();
    	deleteAllWithPrefix(root, prefix, 0, returnSet);
    	return returnSet;
    }
    
    private Node<Value> deleteAllWithPrefix(Node<Value> x, String prefix, int d, Set<Value> set) {
    	
    	if (x == null) {
    		return x;
    	}
    	
    	if (d < prefix.length()) {
    		char c = prefix.charAt(d);
    		x.links[Character.getNumericValue(c)] = deleteAllWithPrefix(x.links[Character.getNumericValue(c)], prefix, d+1, set);
    	}
    	
    	if (d == prefix.length()) {
    		set.addAll(x.valueSet);
        	x.valueSet.clear();
        	for (int i = 0; i < x.links.length; i++) {
        		if (x.links[i]!=null) {
            		x.links[i] = deleteAllWithPrefix(x.links[i], prefix, d, set);
        		}
        	}
    	}
//need to determine return value to decide whether to prune
//if valueSet is not empty we know branch must be kept
    	if (!x.valueSet.isEmpty()) {
    		return x;
    	}
//if valueSet is not empty, must have live children or else will be pruned
    	for (int i = 0; i < x.links.length; i++) {
    		if (x.links[i] != null) {
    			return x;
    		}
    	}
    	return null;
    	
    }
    
    public Value delete(String key, Value val) {
    	if (key == null||val == null) {
    		throw new IllegalArgumentException();
    	}
    	if (key.contains(" ")) {
    		throw new IllegalArgumentException();
    	}
    	if (key== "") {
    		return null;
    	}
    	Set<Value> returnSet = new HashSet<>();
    	delete(root, key, 0, val, returnSet);
//if set is not empty return value must be the first (and only) value in the set
    	return returnSet.isEmpty() ? null : returnSet.iterator().next();
    	
    }
    
    private Node<Value> delete(Node<Value> x, String key, int d, Value val, Set<Value> set) {
    	//if can't track a path to the string return early
    	if (x == null) {
    		return x;
    	}
    	
    	if (d < key.length()) {
    		char c = key.charAt(d);
    		x.links[Character.getNumericValue(c)] = delete(x.links[Character.getNumericValue(c)], key, d+1, val, set);
    	}
    	
    	if (d == key.length()) {
    		if(x.valueSet.contains(val)) {
 //need to return the value in the node set
 //will iterate through and if it finds it add it to the passed set and remove it from the backing set
    			Iterator<Value> iterator = x.valueSet.iterator();
    			while(iterator.hasNext()) {
    				Value value = iterator.next();
    				if (val.equals(value)) {
    					set.add(value);
    					iterator.remove();
    					break;
    				}
    			}
    		}
    	}
    	if (!x.valueSet.isEmpty()) {
    		return x;
    	}
    	for (int i = 0; i < x.links.length; i++) {
    		if (x.links[i] != null) {
    			return x;
    		}
    	}
    	return null;
    }
    
//    public void printEverything() {
//    	printEverything(root, "");
//    }
//	
//    private void printEverything(Node<Value> node, String string) {
//    	if (!node.valueSet.isEmpty()) {
//    		System.out.println(string+": "+node.valueSet);
//    	}
//    	for (int i = 0; i < node.links.length; i++) {
//    		if (node.links[i] != null) {
//    			printEverything(node.links[i], string+Character.forDigit(i, 36));
//    		}
//    	}
//    	
//    }
    
//    public void ensureNoBarrenBranches() {
//    	ensureNoBarrenBranches(root, "");
//    }
//    
//    private void ensureNoBarrenBranches(Node<Value> node, String string) {
//		boolean bool = false;
//    	for (int i = 0; i < node.links.length; i++) {
//    		if (node.links[i] != null) {
//    			ensureNoBarrenBranches(node.links[i], string+Character.forDigit(i, 36));
//    			bool = true;
//    		}
//    	}
//    	if (bool) {
//    		return;
//    	}
//    	if (node.valueSet.isEmpty()&&node!=root) {
//    		throw new IllegalStateException(string+" is barren and non pruned!!");
//    	}
//    }
    
	@SuppressWarnings("hiding")
	private class Node<Value> {
		private Set<Value> valueSet;
		private Node<Value>[] links;
		
		@SuppressWarnings("unchecked")
		Node() {
			valueSet = new HashSet<Value>();
			links = new Node[alphabetSize];
		}
	}
}
