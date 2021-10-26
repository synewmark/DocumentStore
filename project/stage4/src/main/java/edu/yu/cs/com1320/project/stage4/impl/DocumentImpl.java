package edu.yu.cs.com1320.project.stage4.impl;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import edu.yu.cs.com1320.project.stage4.Document;

public class DocumentImpl implements Document {
	private long lastUsedTime;
	private final URI uri;
	private final Map<String, Integer> map = new HashMap<>();
	private final String txt;
	private final byte[] binaryData;
	public DocumentImpl(URI uri, String txt) {
		if (uri==null||txt==null||uri.toString()==""||txt.length() == 0) {
			throw new IllegalArgumentException();
		}
		this.uri = uri;
		this.txt = txt;
		this.binaryData = null;
		
		for (String word : sanitize(txt).trim().split("\\s+")) {
			map.put(word, map.getOrDefault(sanitize(word), 0)+1);
		}
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
	@Override
	public int wordCount(String word) {
		return map.getOrDefault(sanitize(word), 0);
	}
	@Override
	public Set<String> getWords() {
		return map.keySet();
	}
	
	@Override
	public int compareTo(Document d) {
		return Long.compare(getLastUseTime(), d.getLastUseTime());
	}
	@Override
	public long getLastUseTime() {
		return lastUsedTime;
	}
	@Override
	public void setLastUseTime(long timeInNanoseconds) {
		lastUsedTime = timeInNanoseconds;
	}
	//sanitize removes all characters not alphanumeric or a space before lowering them to lower case
	//sanitize *before* splitting into words to protect against edge case where word is completely comprised of illegal characters
	private String sanitize(String string) {
		return string.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase();
	}	
}