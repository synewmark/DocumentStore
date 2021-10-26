package edu.yu.cs.com1320.project.stage1.impl;
import java.net.URI;

import edu.yu.cs.com1320.project.stage1.Document;
public class DocumentImpl implements Document {
	private final URI uri;
	private final String txt;
	private final byte[] binaryData;
	public DocumentImpl(URI uri, String txt) {
		if (uri==null||txt==null||uri.toString()==""||txt.length() == 0) {
			throw new IllegalArgumentException();
		}
		this.uri = uri;
		this.txt = txt;
		this.binaryData = null;
	}
	public DocumentImpl(URI uri, byte[] binaryData) {
		if (uri==null||binaryData==null||uri.toString()==""||binaryData.length == 0) {
			throw new IllegalArgumentException();
		}
		this.uri = uri;
		this.binaryData = binaryData;
		this.txt = null;
	}
	public byte[] getDocumentBinaryData() {
		return binaryData;
	}
	public String getDocumentTxt() {
		return txt;
	}
	public URI getKey() {
		return uri;
	}
	@Override
	public boolean equals(Object object) {
		//I continue to hate not instance of formatting rules
		if (! (object instanceof DocumentImpl)) {
			return false;
		}
		return object.hashCode()==this.hashCode();
	}
	@Override
	public int hashCode() {
		//String.hashCode is consistent with contents (i.e. new String("Hello").hashCode()==new String("Hello").hashCode()) but array is based on memory address (like most Objects)
		//need Array.hashCode() to get deep hashCode() and consistent results based on values in array
	    int result = uri.hashCode();
	    result = 31 * result + (txt != null ? txt.hashCode() : 0);
	    result = 31 * result + java.util.Arrays.hashCode(binaryData);
	    return result;
	}
	
}