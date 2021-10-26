package edu.yu.cs.com1320.project.stage2.impl;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Function;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.stage2.Document;
import edu.yu.cs.com1320.project.stage2.DocumentStore;
import edu.yu.cs.com1320.project.Command;


public class DocumentStoreImpl implements DocumentStore {
	HashTable<URI, Document> hashtable;
	Stack<Command> stack;
	public DocumentStoreImpl() {
		hashtable = new HashTableImpl<>();
		stack = new StackImpl<>();
	}
	
    public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException {
    	//throw exception if any 
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
    	Function<URI, Boolean> function = (URI)-> this.privatePutDocument(uri, returnDoc);
    	Command newCommand = new Command(uri, function);
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
    	Document returnDoc = hashtable.put(uri, null);
    	Function<URI, Boolean> function = (URI)-> this.privatePutDocument(uri, returnDoc);
    	//if return doc is null function should do nothing
    	if (returnDoc == null) {
    		function = (URI)-> true;
    	}
    	Command newCommand = new Command(uri, function);
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
    
    public void undo(URI uri) throws IllegalStateException {
    	Command undoCommand = null;
    	Stack<Command> newStack = new StackImpl<>();
    	while (stack.peek()!=null) {
    		Command currCommand = stack.pop();
    		if (currCommand.getUri().equals(uri)) {
    			undoCommand = currCommand;
    			break;
    		}
    		newStack.push(currCommand);
    	}
    	restack(stack, newStack);
    	if (undoCommand == null) {
    		throw new IllegalStateException();
    	}
    	undoCommand.undo();
    }
    
    private boolean privatePutDocument(URI uri, Document document) {
    	hashtable.put(uri, document);
    	return true;
    }
    
    //puts all the elements in stack 2 into stack 1 in reverse order
    private void restack(Stack<Command> stack1,  Stack<Command> stack2) {
    	while(stack2.peek()!=null) {
    		stack1.push(stack2.pop());
    	}
    }
}