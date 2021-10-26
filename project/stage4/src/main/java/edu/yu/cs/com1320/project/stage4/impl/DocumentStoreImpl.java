package edu.yu.cs.com1320.project.stage4.impl;

import static java.lang.System.nanoTime;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Function;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.DocumentStore;
import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.CommandSet;


public class DocumentStoreImpl implements DocumentStore {
	private final HashTable<URI, Document> hashtable;
	private final Stack<Undoable> stack;
	private final Trie<Document> trie;
	private final MinHeap<Document> heap;
	private int currentBytesUsed;
	private int currentDocumentCount;
	private int maxBytes;
	private int maxDocumentCount;
	private long timeStamp;
	public DocumentStoreImpl() {
		hashtable = new HashTableImpl<>();
		stack = new StackImpl<>();
		trie = new TrieImpl<>();
		heap = new MinHeapImpl<>();
		currentBytesUsed = 0;
		currentDocumentCount = 0;
		maxBytes = -1;
		maxDocumentCount = -1;
		timeStamp = 0;
	}
	@Override
    public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException {
    	//throw exception if any inputs are null
    	if (uri == null || format == null ) {
    		throw new IllegalArgumentException();
    	}
    	//like HashTableImpl calls delete if passed null
    	if (input == null) {
    		return deleteDocumentInt(uri);
    	}
    	byte[] documentBytes = input.readAllBytes();
    	//checks format and creates DocumentImpl of the requisite type: i.e. binary or string
    	//this is one of those things I'm probably going to look back on and hate
    	//the ternary operator paradigm is too tempting here but it results in a line 160 char long
    	//kind of quaint that I was so bugged by doing this in hindsight - later code is way worse
    	Document doc = format == DocumentStore.DocumentFormat.BINARY ? new DocumentImpl(uri, documentBytes) : new DocumentImpl(uri, new String(documentBytes));
    	//store doc currently in hashtable for undo because privatePutDocument will overwrite it
    	Document returnDoc = hashtable.get(uri);
    	timeStamp = nanoTime();
    	//privatePutDocument makes space for the document and updates curentBytes/currentCount
    	privatePutDocument(uri, doc);
    	Undoable newCommand = new GenericCommand<URI>(uri, (URI)-> this.privatePutDocument(uri, returnDoc));
    	stack.push(newCommand);
    	return returnDoc==null ? 0 : returnDoc.hashCode();
    }
	@Override
    public Document getDocument(URI uri) {
    	Document returnDoc = hashtable.get(uri);
    	if (returnDoc != null) {
    		returnDoc.setLastUseTime(nanoTime());
    		heap.reHeapify(returnDoc);
    	}
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
    	Document returnDoc = hashtable.get(uri);
    	timeStamp = nanoTime();
    	privatePutDocument(uri, null);
    	Function<URI, Boolean> function = (URI)-> this.privatePutDocument(uri, returnDoc);
    	//if return doc is null function should do nothing
    	if (returnDoc == null) {
    		function = (URI)-> true;
    	}
    	Undoable newCommand = new GenericCommand<URI>(uri, function);
    	stack.push(newCommand);
    	//returns 0 if null otherwise Object's hashCode
    	return returnDoc==null ? 0 : returnDoc.hashCode();
    }
    @Override
    public void undo() throws IllegalStateException {
    	if (stack.size() == 0) {
    		throw new IllegalStateException();
    	}
    	timeStamp = nanoTime();
    	//heap doesn't have a delete target method need to update document's lastUseTime to push it to top and delete top

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
    	List<Document> returnList = trie.getAllSorted(sanitizedString, new Comparator<Document>() {
			@Override
			public int compare(Document doc1, Document doc2) {
				return Integer.compare(doc2.wordCount(sanitizedString), doc1.wordCount(sanitizedString));
			}
		});
    	timeStamp = nanoTime();
    	for (Document document : returnList) {
    		document.setLastUseTime(nanoTime());
    		heap.reHeapify(document);
    	}
    	return returnList;
    }
    @Override
    public List<Document> searchByPrefix(String prefix) {
    	if (prefix==null) {
    		throw new IllegalArgumentException();
    	}
//need to declare a new String to get 'effectively final' for the Lambda
    	String sanitizePrefix = sanitize(prefix);
    	List<Document> returnList = trie.getAllWithPrefixSorted(sanitizePrefix, new Comparator<Document>() {
			@Override
			public int compare(Document doc1, Document doc2) {
				int docCount1 = 0;
				int docCount2 = 0;
				for (String word : doc1.getWords()) {
					if (word.startsWith(sanitizePrefix)) {
						docCount1++;
					}
				}
				
				for (String word : doc2.getWords()) {
					if (word.startsWith(sanitizePrefix)) {
						docCount2++;
					}
				}
				return Integer.compare(docCount2, docCount1);
			}
		});
    	timeStamp = nanoTime();
    	for (Document document : returnList) {
    		document.setLastUseTime(timeStamp);
    		heap.reHeapify(document);
    	}
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
    	for (Document document : trie.deleteAll(keyword)) {
//in order: 1. delete the document from the HashTable, 2. delete the document from the trie
//3. add the document's URI to the return set, 3. add a new GenericCommand to the CommandSet
    		privatePutDocument(document.getKey(), null);
    		uriSet.add(document.getKey());
    		commandset.addCommand(new GenericCommand<URI>(document.getKey(), (URI)->this.privatePutDocument(document.getKey(), document)));
    		
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
    	for (Document document : trie.deleteAllWithPrefix(prefix)) {
//in order: 1. delete the document from the HashTable, 2. delete the document from the trie
//3. add the document's URI to the return set, 3. add a new GenericCommand to the CommandSet
    		privatePutDocument(document.getKey(), null);
    		uriSet.add(document.getKey());
    		commandset.addCommand(new GenericCommand<URI>(document.getKey(), (URI)->this.privatePutDocument(document.getKey(), document)));
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
		while (currentDocumentCount > maxDocumentCount) {
			//deleteLeastUsedDocument does update currentDocumentCount
			deleteLeastUsedDocument();
		}
	}

	@Override
	public void setMaxDocumentBytes(int limit) {
		if (limit < 0) {
    		throw new IllegalArgumentException();
    	}
		maxBytes = limit;
		//if limit is lowered may need to delete documents
		while (currentBytesUsed > maxBytes) {
			//deleteLeastUsedDocument does update currentDocumentCount
			deleteLeastUsedDocument();
		}
	}
    
    //in order to keep just one private put method need some edge case testing
    //note despite name is also responsible for deleting
	//every call to privatePutDocument *must* be accompanied by an update to the global timeStamp field just prior!
    private boolean privatePutDocument(URI uri, Document document) {
    	//first check if old document already exists in documentStore - if it does delete it from the trie and heap
    	//regardless of whether this is a 'replace' or a delete, the previous document must be deleted.
    	Document prevDocument = hashtable.get(uri);
    	if (prevDocument != null) {
    		trieDelete(prevDocument);
    		//heap doesn't have a delete target method need to update document's lastUseTime to push it to top and delete top
    		prevDocument.setLastUseTime(Long.MIN_VALUE);
    		heap.reHeapify(prevDocument);
    		heap.remove();
    		prevDocument.setLastUseTime(timeStamp);
    		currentBytesUsed -= documentSize(prevDocument);
    		currentDocumentCount--;
    	}
    	//if document does not equal null i.e. it's not just performing a delete, put the document's words in the trie and the heap
    	if (document!=null) {
    		makeSpaceForDocument(document);
            triePut(document);
            document.setLastUseTime(timeStamp);
            heap.insert(document);
            currentBytesUsed += documentSize(document);
    		currentDocumentCount++;
    	}
    	//regardless of whether replacing or deleting this command will fulfill it
    	hashtable.put(uri, document);
    	return true;
    	}
    //iterates through document's words and puts each in the trie
    //not null safe!
    private void triePut(Document document) {
    	for (String word : document.getWords()) {
    		trie.put(word, document);
    	}
    }
    //iterates through document's words and deletes each from trie
    //not null safe!
    private void trieDelete(Document document) {
    	for (String word : document.getWords()) {
    		trie.delete(word, document);
    	}
    }
    
    //puts all the elements in stack 2 into stack 1 in reverse order
    private void restack(Stack<Undoable> stack1,  Stack<Undoable> stack2) {
    	while(stack2.peek()!=null) {
    		stack1.push(stack2.pop());
    	}
    }
    //deletes documents until there's room for document
    private void makeSpaceForDocument(Document document) {
    	int documentSize = documentSize(document);
    	//throw exception if no amount of deleting will make enough space: i.e. if max size is smaller than document or documentCount is less than 1
    	//both max values don't start initialized. They're set to -1 in the constructor which corresponds to unlimited 
    	if ((maxBytes != -1 && documentSize > maxBytes) || (maxDocumentCount != -1 &&maxDocumentCount < 1)) {
    		throw new IllegalStateException();
    	}
    	
    	while ((maxBytes != -1) && (currentBytesUsed+documentSize > maxBytes) || (maxDocumentCount != -1) && (currentDocumentCount+1 > maxDocumentCount)) {
    		deleteLeastUsedDocument();
    	}
    }
    
    private void deleteLeastUsedDocument() {
//call heap.remove to find document to delete, because heap lacks 'peek' method. Call privatePutDocument with null argument to delete it from everywhere but stack
//finally, delete uri in document from undo stack per the API
    	Document deleteDocument = heap.remove();
    	heap.insert(deleteDocument);
    	timeStamp = nanoTime();
		privatePutDocument(deleteDocument.getKey(), null);
		deleteURIFromStack(deleteDocument.getKey());
    }
    
    @SuppressWarnings("unchecked")
	private void deleteURIFromStack(URI uri) {
    	Stack<Undoable> stack1 = new StackImpl<>();
    	while (stack.peek()!=null) {
    		Undoable undoable = stack.pop();
    		if (undoable instanceof GenericCommand) {
    			if (!((GenericCommand<URI>) undoable).getTarget().equals(uri)) {
    				stack1.push(undoable);
    			}
    		} else {
    			if (((CommandSet<URI>) undoable).containsTarget(uri)) {
    				Iterator<GenericCommand<URI>> iterator = ((CommandSet<URI>) undoable).iterator();
//while loop runs until iterator.next's target equals the uri
//only works because we know the set contains the target, otherwise would result in exception
    				while(!iterator.next().getTarget().equals(uri));
//after reaching correct uri call iterator.remove which removes the most recently accessed value
    				iterator.remove();
    			}
//commandset might be empty as a result of the operation we may of committed
//if it isn't add it back to the undo set
    			if (!((CommandSet<URI>) undoable).isEmpty()) {
					stack1.push(undoable);
				}
    		}
    	}
    	restack(stack, stack1);
    }
    //sanitize string by removing all non alphanumeric characters and moving everything to lower case
    //note distinction between this sanitize and DocumentImpl's
    private String sanitize(String string) {
		return string.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
	}
    
    //checks document's size - if it's binary doc return the bitarray length, if it's a string doc return the length of the string's bit array
    private int documentSize(Document document) {
    	return document.getDocumentBinaryData() == null ?  document.getDocumentTxt().getBytes().length : document.getDocumentBinaryData().length;
    }
}
