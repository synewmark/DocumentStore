package edu.yu.cs.com1320.project.stage1.impl;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage1.Document;
import edu.yu.cs.com1320.project.stage1.DocumentStore;

public class DocumentStoreImpl implements DocumentStore {
	HashTable<URI, Document> hashtable = new HashTableImpl<>();
	public DocumentStoreImpl() {
		hashtable = new HashTableImpl<>();
	}
    public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException {
    	//like HashTableImpl calls delete if passed null
    	if (input == null) {
    		return deleteDocumentInt(uri);
    	}
    	Document returnDoc;
    	byte[] documentBytes = input.readAllBytes();
    	if (format == DocumentStore.DocumentFormat.BINARY) {
    		Document byteDoc = new DocumentImpl(uri, documentBytes);
    		returnDoc = hashtable.put(uri, byteDoc);
    	} else  {
    		Document stringDoc = new DocumentImpl(uri, new String(documentBytes));
    		returnDoc = hashtable.put(uri, stringDoc);
    	}
    	return returnDoc==null ? 0 : returnDoc.hashCode();
    }
    public Document getDocument(URI uri) {
    	return hashtable.get(uri);
    }
    public boolean deleteDocument(URI uri) {
    	//calls int version and returns true if not 0
    	return deleteDocumentInt(uri)==0 ? false : true;
    }
    private int deleteDocumentInt(URI uri) {
    	//need to check if returnValue is null and return it, need variable to keep it in scope
    	Document returnValue = hashtable.put(uri, null);
    	//returns 0 if null otherwise Object's hashCode
    	return returnValue==null ? 0 : returnValue.hashCode();
    }
}
