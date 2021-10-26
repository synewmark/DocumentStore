package edu.yu.cs.com1320.project.stage3.impl;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Function;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.stage3.DocumentStore;
import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.CommandSet;


public class DocumentStoreImpl implements DocumentStore {
	final HashTable<URI, Document> hashtable;
	final Stack<Undoable> stack;
	final Trie<Document> trie;
	public DocumentStoreImpl() {
		hashtable = new HashTableImpl<>();
		stack = new StackImpl<>();
		trie = new TrieImpl<>();
	}
	
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
    	Document doc = format == DocumentStore.DocumentFormat.BINARY ? new DocumentImpl(uri, documentBytes) : new DocumentImpl(uri, new String(documentBytes));
    	Document returnDoc = hashtable.put(uri, doc);
    	if (returnDoc != null) {
    		trieDelete(returnDoc);
    	}
    	triePut(doc);
    	Undoable newCommand = new GenericCommand<URI>(uri, (URI)-> this.privatePutDocument(uri, returnDoc));
    	stack.push(newCommand);
    	return returnDoc==null ? 0 : returnDoc.hashCode();
    }
    
    public Document getDocument(URI uri) {
    	return hashtable.get(uri);
    }
    
    public boolean deleteDocument(URI uri) {
    	if (uri == null) {
    		throw new IllegalArgumentException();
    	}
    	//calls int version and returns true if not 0
    	return deleteDocumentInt(uri)==0 ? false : true;
    }
    private int deleteDocumentInt(URI uri) {
    	if (uri==null) {
    		throw new IllegalArgumentException();
    	}
    	Document presentDocument = hashtable.get(uri);
    	if (presentDocument!=null) {
    		trieDelete(presentDocument);
    	}
    	Document returnDoc = hashtable.put(uri, null);
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
    
    public void undo() throws IllegalStateException {
    	if (stack.size() == 0) {
    		throw new IllegalStateException();
    	}
    	//pops top element of command stack and runs it
    	stack.pop().undo();
    }
    
    @SuppressWarnings("unchecked")
	public void undo(URI uri) throws IllegalStateException {
    	if (uri==null) {
    		throw new IllegalArgumentException();
    	}
    	boolean undone = false;
    	Stack<Undoable> newStack = new StackImpl<>();
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
    
    public List<Document> search(String string) {
    	if (string==null) {
    		throw new IllegalArgumentException();
    	}
//need to declare a new String to get 'effectively final' for the Lambda
//two mistakes were made in previous version. 1. I copied the code from searchByPrefix directly even calling the same trie method
//2. there was no need to make such a complex comparator to begin with. Document.wordCount does exactly what I want as seen below
    	String sanitizedString = sanitize(string);
    	return trie.getAllSorted(sanitizedString, new Comparator<Document>() {
			@Override
			public int compare(Document doc1, Document doc2) {
				return Integer.compare(doc1.wordCount(sanitizedString), doc2.wordCount(sanitizedString));
			}
		});
    }
    
    public List<Document> searchByPrefix(String prefix) {
    	if (prefix==null) {
    		throw new IllegalArgumentException();
    	}
//need to declare a new String to get 'effectively final' for the Lambda
    	String sanitizePrefix = sanitize(prefix);
    	return trie.getAllWithPrefixSorted(sanitizePrefix, new Comparator<Document>() {
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
    }
    
    public Set<URI> deleteAll(String keyword){
    	if (keyword==null) {
    		throw new IllegalArgumentException();
    	}
    	Set<URI> uriSet = new HashSet<URI>();
    	CommandSet<URI> commandset = new CommandSet<URI>();
//if keyword is not present in a the document will return an empty collection so for-each loop will simply not run
    	for (Document document : trie.deleteAll(keyword)) {
//in order: 1. delete the document from the HashTable, 2. delete the document from the trie
//3. add the document's URI to the return set, add a new GenericCommand to the CommandSet
    		hashtable.put(document.getKey(), null);
    		trieDelete(document);
    		uriSet.add(document.getKey());
    		commandset.addCommand(new GenericCommand<URI>(document.getKey(), (URI)->this.privatePutDocument(document.getKey(), document)));
    	}
//if nothing was deleted still push the ineffectual delete to the undo stack per API
    	stack.push(commandset);
    	return uriSet;
    }
    
    public Set<URI> deleteAllWithPrefix(String prefix){
    	if (prefix==null) {
    		throw new IllegalArgumentException();
    	}
    	Set<URI> uriSet = new HashSet<URI>();
    	CommandSet<URI> commandset = new CommandSet<URI>();
//if keyword is not present in a the document will return an empty collection so for-each loop will simply not run
    	for (Document document : trie.deleteAllWithPrefix(prefix)) {
//in order: 1. delete the document from the HashTable, 2. delete the document from the trie
//3. add the document's URI to the return set, add a new GenericCommand to the CommandSet
    		hashtable.put(document.getKey(), null);
    		trieDelete(document);
    		uriSet.add(document.getKey());
    		commandset.addCommand(new GenericCommand<URI>(document.getKey(), (URI)->this.privatePutDocument(document.getKey(), document)));
    	}
//if nothing was deleted still push the ineffectual delete to the undo stack per API
    	stack.push(commandset);
    	return uriSet;
    }
    //in order to keep just one private put method need some edge case testing
    //note despite name is also responsible for deleting
    private boolean privatePutDocument(URI uri, Document document) {
    	//first check if old document already exists in documentStore - if it does delete it from the trie
    	if (hashtable.get(uri)!=null) {
    		trieDelete(hashtable.get(uri));
    	}
    	//if document does not equal null i.e. it's not just performing a delete, put the document's words in the trie
    	if (document!=null) {
            triePut(document);
    	}
    	//regardless of whether replacing or deleting this command will fulfill it
    	hashtable.put(uri, document);
    	return true;
    }
    //iterates through document's words and deletes each from trie
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
    //sanitize string by removing all non alphanumeric characters and moving everything to lower case
    //not distinction between this sanitize and DocumentImpl
    private String sanitize(String string) {
		return string.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
	}
}