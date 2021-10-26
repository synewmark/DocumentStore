package edu.yu.cs.com1320.project.impl;

import java.io.IOException;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

public class BTreeImpl<Key extends Comparable<Key>, Value> implements BTree<Key, Value>{
	    //max children per B-tree node = MAX-1 (must be an even number and greater than 2)
	    private static final int MAX = 4;
	    private Node root; //root of the B-tree
	    private int height; //height of the B-tree
	    private int n; //number of key-value pairs in the B-tree
	    private PersistenceManager<Key, Value> pm;
	    //place dummy in value fields to signify that file is on disk
	    private final Object dummy = new Object();

	    //B-tree node data type
	    private final class Node
	    {
	        private int entryCount; // number of entries
	        @SuppressWarnings("unchecked")
			private Entry<Key, Value>[] entries = new Entry[BTreeImpl.MAX]; // the array of children
	        private Node next;
	        private Node previous;

	        // create a node with k entries
	        private Node(int k)
	        {
	            this.entryCount = k;
	        }

	        private void setNext(Node next)
	        {
	            this.next = next;
	        }
	        private Node getNext()
	        {
	            return this.next;
	        }
	        private void setPrevious(Node previous)
	        {
	            this.previous = previous;
	        }
	        @SuppressWarnings("unused")
			private Node getPrevious()
	        {
	            return this.previous;
	        }


	    }

	    //internal nodes: only use key and child
	    //external nodes: only use key and value
	    @SuppressWarnings("hiding")
		private final class Entry<Key, Value>
	    {
	        private Key key;
	        private Value val;
	        private Node child;

	        public Entry(Key key, Value val, Node child)
	        {
	            this.key = key;
	            this.val = val;
	            this.child = child;
	        }
	    }

	    /**
	     * Initializes an empty B-tree.
	     */
	    public BTreeImpl()
	    {
	        this.root = new Node(0);
	    }
	    
	    public void setPersistenceManager(PersistenceManager<Key,Value> pm) {
	    	this.pm = pm;
	    }
	    
	    public Value get(Key key)
	    {
	        if (key == null)
	        {
	            throw new IllegalArgumentException("argument to get() is null");
	        }
	        Entry<Key, Value> entry = this.get(this.root, key, this.height);
	        if(entry != null)
	        {
	            if (entry.val == dummy) {
	            	try {
						entry.val = pm.deserialize(key);
						return entry.val;
					} catch (IOException e) {
						e.printStackTrace();
					}
	            }
	            return entry.val;
	        }
	        return null;
	    }
	        

	    private Entry<Key, Value> get(Node currentNode, Key key, int height)
	    {
	        Entry<Key, Value>[] entries = currentNode.entries;

	        //current node is external (i.e. height == 0)
	        if (height == 0)
	        {
	            for (int j = 0; j < currentNode.entryCount; j++)
	            {
	                if(isEqual(key, entries[j].key))
	                {
	                    //found desired key. Return its value
	                    return entries[j];
	                }
	            }
	            //didn't find the key
	            return null;
	        }

	        //current node is internal (height > 0)
	        else
	        {
	            for (int j = 0; j < currentNode.entryCount; j++)
	            {
	                //if (we are at the last key in this node OR the key we
	                //are looking for is less than the next key, i.e. the
	                //desired key must be in the subtree below the current entry),
	                //then recurse into the current entry’s child
	                if (j + 1 == currentNode.entryCount || less(key, entries[j + 1].key))
	                {
	                    return this.get(entries[j].child, key, height - 1);
	                }
	            }
	            //didn't find the key
	            return null;
	        }
	    }
	    
	    public Value put(Key key, Value val)
	    {
	        if (key == null)
	        {
	            throw new IllegalArgumentException("argument key to put() is null");
	        }
	        //if the key already exists in the b-tree, simply replace the value
	        Entry<Key, Value> alreadyThere = this.get(this.root, key, this.height);
	        if(alreadyThere != null)
	        {
	        	Value returnVal = alreadyThere.val;
	        	if (returnVal == dummy) {
	        		try {
						returnVal = pm.deserialize(key);
					} catch (IOException e) {
						e.printStackTrace();
					}
	        	}
	            alreadyThere.val = val;
	            return returnVal;
	        }

	        Node newNode = this.put(this.root, key, val, this.height);
	        this.n++;
	        if (newNode == null)
	        {
	            return null;
	        }

	        //split the root:
	        //Create a new node to be the root.
	        //Set the old root to be new root's first entry.
	        //Set the node returned from the call to put to be new root's second entry
	        Node newRoot = new Node(2);
	        newRoot.entries[0] = new Entry<Key, Value>(this.root.entries[0].key, null, this.root);
	        newRoot.entries[1] = new Entry<Key, Value>(newNode.entries[0].key, null, newNode);
	        this.root = newRoot;
	        //a split at the root always increases the tree height by 1
	        this.height++;
			return null;
	    }

	    
	    /**
	     *
	     * @param currentNode
	     * @param key
	     * @param val
	     * @param height
	     * @return null if no new node was created (i.e. just added a new Entry into an existing node). If a new node was created due to the need to split, returns the new node
	     */
	    private Node put(Node currentNode, Key key, Value val, int height)
	    {
	        int j;
	        Entry<Key, Value> newEntry = new Entry<>(key, val, null);

	        //external node
	        if (height == 0)
	        {
	            //find index in currentNode’s entry[] to insert new entry
	            //we look for key < entry.key since we want to leave j
	            //pointing to the slot to insert the new entry, hence we want to find
	            //the first entry in the current node that key is LESS THAN
	            for (j = 0; j < currentNode.entryCount; j++)
	            {
	                if (less(key, currentNode.entries[j].key))
	                {
	                    break;
	                }
	            }
	        }

	        // internal node
	        else
	        {
	            //find index in node entry array to insert the new entry
	            for (j = 0; j < currentNode.entryCount; j++)
	            {
	                //if (we are at the last key in this node OR the key we
	                //are looking for is less than the next key, i.e. the
	                //desired key must be added to the subtree below the current entry),
	                //then do a recursive call to put on the current entry’s child
	                if ((j + 1 == currentNode.entryCount) || less(key, currentNode.entries[j + 1].key))
	                {
	                    //increment j (j++) after the call so that a new entry created by a split
	                    //will be inserted in the next slot
	                    Node newNode = this.put(currentNode.entries[j++].child, key, val, height - 1);
	                    if (newNode == null)
	                    {
	                        return null;
	                    }
	                    //if the call to put returned a node, it means I need to add a new entry to
	                    //the current node
	                    newEntry.key = newNode.entries[0].key;
	                    newEntry.val = null;
	                    newEntry.child = newNode;
	                    break;
	                }
	            }
	        }
	        //shift entries over one place to make room for new entry
	        for (int i = currentNode.entryCount; i > j; i--)
	        {
	            currentNode.entries[i] = currentNode.entries[i - 1];
	        }
	        //add new entry
	        currentNode.entries[j] = newEntry;
	        currentNode.entryCount++;
	        if (currentNode.entryCount < BTreeImpl.MAX)
	        {
	            //no structural changes needed in the tree
	            //so just return null
	            return null;
	        }
	        else
	        {
	            //will have to create new entry in the parent due
	            //to the split, so return the new node, which is
	            //the node for which the new entry will be created
	            return this.split(currentNode, height);
	        }
	    }
	    
	    @SuppressWarnings("unchecked")
		public void moveToDisk(Key k) throws Exception {
	        Entry<Key, Value> entry = this.get(this.root, k, this.height);
	        if (entry == null || entry.val == null) {
	        	throw new IllegalArgumentException();
	        }
	    	pm.serialize(k, entry.val);
	    	//need to put dummy object, this paradigm necessitates if checks on all entry value accesses
	    	//don't have any better options though
	    	entry.val = (Value) dummy;
	    }
	    
	    /**
	     * split node in half
	     * @param currentNode
	     * @return new node
	     */
	    private Node split(Node currentNode, int height)
	    {
	        Node newNode = new Node(BTreeImpl.MAX / 2);
	        //by changing currentNode.entryCount, we will treat any value
	        //at index higher than the new currentNode.entryCount as if
	        //it doesn't exist
	        currentNode.entryCount = BTreeImpl.MAX / 2;
	        //copy top half of h into t
	        for (int j = 0; j < BTreeImpl.MAX / 2; j++)
	        {
	            newNode.entries[j] = currentNode.entries[BTreeImpl.MAX / 2 + j];
	        }
	        //external node
	        if (height == 0)
	        {
	            newNode.setNext(currentNode.getNext());
	            newNode.setPrevious(currentNode);
	            currentNode.setNext(newNode);
	        }
	        return newNode;
	    }
	    
	    private boolean less(Key k1, Key k2)
	    {
	        return k1.compareTo(k2) < 0;
	    }

	    private boolean isEqual(Key k1, Key k2)
	    {
	        return k1.compareTo(k2) == 0;
	    }
	    
}
