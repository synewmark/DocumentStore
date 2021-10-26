package edu.yu.cs.com1320.project.stage5.impl;

import static java.lang.System.nanoTime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Function;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.CommandSet;


public class DocumentStoreImpl implements DocumentStore {
	private final Function<URI, Document> getterFunct;
	private final BTree <URI, Document> btree;
	private final Stack<Undoable> stack;
	private final Trie<URI> trie;
	private final MinHeap<DocumentImit> heap;
	private final Set<DocumentImit> onMemory;
	private int currentBytesUsed;
	private int currentDocumentCount;
	private int maxBytes;
	private int maxDocumentCount;
	private long timeStamp;
	public DocumentStoreImpl(File file) {
		btree = new BTreeImpl<>();
		stack = new StackImpl<>();
		trie = new TrieImpl<>();
		heap = new MinHeapImpl<>();
		onMemory = new HashSet<>();
		currentBytesUsed = 0;
		currentDocumentCount = 0;
		maxBytes = -1;
		maxDocumentCount = -1;
		timeStamp = 0;
		getterFunct = btree::get;
		btree.setPersistenceManager(new DocumentPersistenceManager(file));
	}
	public DocumentStoreImpl() {
		this(null);
	}
	@Override
    public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException {
    	//throw exception if any inputs are null
    	if (uri == null || format == null ) {
    		throw new IllegalArgumentException();
    	}
    	if (input == null) {
    		return deleteDocumentInt(uri);
    	}
    	byte[] documentBytes = input.readAllBytes();
    	//checks format and creates DocumentImpl of the requisite type: i.e. binary or string
    	//this is one of those things I'm probably going to look back on and hate
    	//the ternary operator paradigm is too tempting here but it results in a line 160 char long
    	//kind of quaint that I was so bugged by doing this in hindsight - later code is way worse
    	Document doc = format == DocumentStore.DocumentFormat.BINARY ? new DocumentImpl(uri, documentBytes) : new DocumentImpl(uri, new String(documentBytes));
    	//store doc currently in btree for undo because privatePutDocument will overwrite it
    	Document returnDoc = btree.get(uri);
    	timeStamp = nanoTime();
    	//privatePutDocument makes space for the document and updates curentBytes/currentCount
    	privatePutDocument(uri, doc);
    	Undoable newCommand = new GenericCommand<URI>(uri, (URI)-> this.privatePutDocument(uri, returnDoc));
    	stack.push(newCommand);
    	return returnDoc==null ? 0 : returnDoc.hashCode();
    }
	@Override
    public Document getDocument(URI uri) {
    	Document returnDoc = btree.get(uri);
    	if (returnDoc != null) {
    		returnDoc.setLastUseTime(nanoTime());
    		DocumentImit imit = new DocumentImit(returnDoc.getKey(), documentSize(returnDoc));;
    		heapPut(imit);
    	}
    	makeSpace();
    	return returnDoc;
    }
	@Override
    public boolean deleteDocument(URI uri) {
    	if (uri == null) {
    		throw new IllegalArgumentException();
    	}
    	//calls int version and returns true if not 0
    	return deleteDocumentInt(uri)!=0;
    }
    private int deleteDocumentInt(URI uri) {
    	if (uri==null) {
    		throw new IllegalArgumentException();
    	}
    	Document returnDoc = btree.get(uri);
    	timeStamp = nanoTime();
    	privatePutDocument(uri, null);
    	stack.push(new GenericCommand<URI>(uri, (URI)-> this.privatePutDocument(uri, returnDoc)));
    	//returns 0 if null otherwise Object's hashCode
    	return returnDoc==null ? 0 : returnDoc.hashCode();
    }
    @Override
    public void undo() throws IllegalStateException {
    	if (stack.size() == 0) {
    		throw new IllegalStateException();
    	}
    	timeStamp = nanoTime();
    	//pops top element of command stack and runs it
    	stack.pop().undo();
    }
    @Override
    @SuppressWarnings("unchecked")
	public void undo(URI uri) throws IllegalStateException {
    	if (uri==null) {
    		throw new IllegalArgumentException();
    	}
    	boolean undone = false;
    	Stack<Undoable> newStack = new StackImpl<>();
    	timeStamp = nanoTime();
    	while (stack.peek()!=null) {
    		Undoable currCommand = stack.pop();
//currCommand can be one of two classes: CommandSet or GenericCommand
//both require casting to check if target is present, and CommandSet requires an additional cast to actually peform the undo
//if I was building this from the ground up I would expand the undoable interface to include a containsTarget
    		if (currCommand instanceof CommandSet<?>) {
    			if (((CommandSet<URI>) currCommand).containsTarget(uri)) {
    				((CommandSet<URI>) currCommand).undo(uri);
    				if (((CommandSet<URI>) currCommand).size() > 0) {
    					newStack.push(currCommand);
    				}
    				undone = true;
    				break;
    			}
    		} else {
    			if (((GenericCommand<URI>) currCommand).getTarget().equals(uri)) {
    				currCommand.undo();
    				undone = true;
    				break;
    			}
    		}
		newStack.push(currCommand);
    	}
    	restack(stack, newStack);
    	if (!undone) {
    		throw new IllegalStateException();
    	}
    }
    @Override
    public List<Document> search(String string) {
    	if (string==null) {
    		throw new IllegalArgumentException();
    	}
//need to declare a new String to get 'effectively final' for the Lambda
    	String sanitizedString = sanitize(string);
    	List<URI> uriList = trie.getAllSorted(sanitizedString, new Comparator<URI>() {
			@Override
			public int compare(URI uri1, URI uri2) {
				return Integer.compare(getterFunct.apply(uri2).wordCount(sanitizedString), getterFunct.apply(uri1).wordCount(sanitizedString));
			}
		});
    	List<Document> returnList = new ArrayList<>();
    	timeStamp = nanoTime();
    	for (URI uri : uriList) {
    		Document document = getterFunct.apply(uri);
    		returnList.add(document);
    		document.setLastUseTime(timeStamp);
    		DocumentImit imit = new DocumentImit(document.getKey(), documentSize(document));
    		heapPut(imit);
    	}
    	makeSpace();
    	return returnList;
    }
    @Override
    public List<Document> searchByPrefix(String prefix) {
    	if (prefix==null) {
    		throw new IllegalArgumentException();
    	}
//need to declare a new String to get 'effectively final' for the Lambda
    	String sanitizePrefix = sanitize(prefix);
    	List<URI> uriList = trie.getAllWithPrefixSorted(sanitizePrefix, new Comparator<URI>() {
			@Override
			public int compare(URI uri1, URI uri2) {
				int docCount1 = 0;
				int docCount2 = 0;
				for (String word : getterFunct.apply(uri1).getWords()) {
					if (word.startsWith(sanitizePrefix)) {
						docCount1++;
					}
				}
				
				for (String word : getterFunct.apply(uri2).getWords()) {
					if (word.startsWith(sanitizePrefix)) {
						docCount2++;
					}
				}
				return Integer.compare(docCount2, docCount1);
			}
		});
    	List<Document> returnList = new ArrayList<>();
    	timeStamp = nanoTime();
    	for (URI uri : uriList) {
    		Document document = getterFunct.apply(uri);
    		returnList.add(document);
    		document.setLastUseTime(timeStamp);
    		DocumentImit imit = new DocumentImit(document.getKey(), documentSize(document));
    		heapPut(imit);
    	}
    	makeSpace();
    	return returnList;
    }
    @Override
    public Set<URI> deleteAll(String keyword){
    	if (keyword==null) {
    		throw new IllegalArgumentException();
    	}
    	Set<URI> uriSet = new HashSet<URI>();
    	CommandSet<URI> commandset = new CommandSet<URI>();
    	timeStamp = nanoTime();
//if keyword is not present in a the document will return an empty collection so for-each loop will simply not run
    	for (URI uri : trie.deleteAll(keyword)) {
//In order: 1. enclose doc in lambda 2. delete doc from all data structures via private put
//3. add doc to uriSet
		Document doc = getterFunct.apply(uri);
    		commandset.addCommand(new GenericCommand<URI>(uri, (URI)->this.privatePutDocument(uri, doc)));
    		privatePutDocument(uri, null);
    		uriSet.add(uri);
    		
    	}
//if nothing was deleted still push the ineffectual delete to the undo stack per API
    	stack.push(commandset);
    	return uriSet;
    }
    @Override
    public Set<URI> deleteAllWithPrefix(String prefix){
    	if (prefix==null) {
    		throw new IllegalArgumentException();
    	}
    	Set<URI> uriSet = new HashSet<URI>();
    	CommandSet<URI> commandset = new CommandSet<URI>();
    	timeStamp = nanoTime();
//if keyword is not present in a the document will return an empty collection so for-each loop will simply not run
    	for (URI uri : trie.deleteAllWithPrefix(prefix)) {
//In order: 1. enclose doc in lambda 2. delete doc from all data structures via private put
//3. add doc to uriSet
		Document doc = getterFunct.apply(uri);
    		commandset.addCommand(new GenericCommand<URI>(uri, (URI)->this.privatePutDocument(uri, doc)));
    		privatePutDocument(uri, null);
    		uriSet.add(uri);
    	}
//if nothing was deleted still push the ineffectual delete to the undo stack per API
    	stack.push(commandset);
    	return uriSet;
    }
    
    @Override
	public void setMaxDocumentCount(int limit) {
    	if (limit < 0) {
    		throw new IllegalArgumentException();
    	}
		maxDocumentCount = limit;
		//if limit is lowered may need to delete documents
		makeSpace();
	}

	@Override
	public void setMaxDocumentBytes(int limit) {
		if (limit < 0) {
    		throw new IllegalArgumentException();
    	}
		maxBytes = limit;
		//if limit is lowered may need to delete documents
		makeSpace();
	}
    
    //in order to keep just one private put method need some edge case testing
    //note despite name is also responsible for deleting
	//every call to privatePutDocument *must* be accompanied by an update to the global timeStamp field just prior!
    private boolean privatePutDocument(URI uri, Document document) {
    	//first check if old document already exists in documentStore - if it does delete it from the trie and heap
    	//regardless of whether this is a 'replace' or a delete, the previous document must be deleted.
    	Document prevDocument = btree.get(uri);
    	if (prevDocument != null) {
    		trieDelete(prevDocument);
    		//heap doesn't have a delete target method need to update document's lastUseTime to push it to top and delete top
    		prevDocument.setLastUseTime(Long.MIN_VALUE);
    		DocumentImit imit = new DocumentImit(prevDocument.getKey(), 0);
    		heap.insert(imit);
    		heap.remove();
    		prevDocument.setLastUseTime(timeStamp);
    		if (onMemory.remove(imit)) {
	    		currentBytesUsed -= documentSize(prevDocument);
	    		currentDocumentCount--;
    		}
    	}
    	btree.put(uri, document);
    	//if document does not equal null i.e. it's not just performing a delete, put the document's words in the trie and the heap
    	if (document!=null) {
            triePut(document);
            document.setLastUseTime(timeStamp);
            DocumentImit imit = new DocumentImit(document.getKey(), documentSize(document));
            heapPut(imit);
            
    	}
    	makeSpace();
    	//regardless of whether replacing or deleting this command will fulfill it
    	return true;
    	}
    //iterates through document's words and puts each in the trie
    //not null safe!
    private void triePut(Document document) {
    	for (String word : document.getWords()) {
    		trie.put(word, document.getKey());
    	}
    }
    //iterates through document's words and deletes each from trie
    //not null safe!
    private void trieDelete(Document document) {
    	for (String word : document.getWords()) {
    		trie.delete(word, document.getKey());
    	}
    }
    
    //puts all the elements in stack 2 into stack 1 in reverse order
    private void restack(Stack<Undoable> stack1,  Stack<Undoable> stack2) {
    	while(stack2.peek()!=null) {
    		stack1.push(stack2.pop());
    	}
    }
    //manages putting into heap
    //checks if imit is already on memory and if it isn't adds it values to currentCounts
    //does not update time or set size in imit, but all call should have already done that
    private void heapPut(DocumentImit imit) {
    	heap.insert(imit);
        if (onMemory.add(imit)) {
            currentBytesUsed += imit.getSize();
    		currentDocumentCount++;
        }
    }
    
    private void makeSpace() {
    	while ((maxBytes != -1) && (currentBytesUsed > maxBytes) || (maxDocumentCount != -1) && (currentDocumentCount > maxDocumentCount)) {
    		moveLeastUsedDocument();
    	}
    }
    
    private void moveLeastUsedDocument() {
    	//remove doc from heap and memory set, move it to disk and then subtract from docCount and memCount
    	DocumentImit deleteDocument = heap.remove();
    	onMemory.remove(deleteDocument);
    	try {
			btree.moveToDisk(deleteDocument.getKey());
		} catch (Exception e) {
			e.printStackTrace();
		}
    	currentDocumentCount--;
    	currentBytesUsed -= deleteDocument.getSize();
    }
    //sanitize string by removing all non alphanumeric characters and moving everything to lower case
    private String sanitize(String string) {
		return string.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
	}
    
    //checks document's size - if it's binary doc return the bitarray length, if it's a string doc return the length of the string's bit array
    private int documentSize(Document document) {
    	return document.getDocumentBinaryData() == null ?  document.getDocumentTxt().getBytes().length : document.getDocumentBinaryData().length;
    }
//need to create imitation class to handle doc when it's in heap because structures other than BTree shouldn't hold them
    private class DocumentImit implements Comparable<DocumentImit> {
    	private final URI uri;
    	//store size because doc is final and to avoid always calling BTree
    	private final int size;
    	
    	private DocumentImit(URI uri, int size) {
    		this.uri = uri;
    		this.size = size;
    	}
    	//both equals and hashcode revolve exclusively around URI
    	@Override
    	public boolean equals(Object obj) {
    		if (! (obj instanceof DocumentImit)) {
    			return false;
    		}
    		return this.uri.equals(((DocumentImit)obj).uri);
    	}
    	@Override
    	//LUT is a long
    	public int compareTo(DocumentImit doc) {
    		return Long.compare(this.getLUT(), doc.getLUT());
    	}
    	//need hahshCode to put in HashSet
    	@Override
    	public int hashCode() {
    		return uri.hashCode();
    	}
    	
    	private URI getKey() {
    		return uri;
    	}
    	
    	private int getSize() {
    		return size;
    	}
    	
    	private long getLUT() {
    		return getterFunct.apply(uri).getLastUseTime();
    	}
    }
}
