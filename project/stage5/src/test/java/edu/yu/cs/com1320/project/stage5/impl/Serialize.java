package edu.yu.cs.com1320.project.stage5.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.junit.jupiter.api.Test;

import edu.yu.cs.com1320.project.stage5.Document;


class Serialize {
		
	File baseDir = new File("C:/Users/ahome/OneDrive/Desktop/Java");
	
	URI uri = URI.create("http://www.yu.edu");
	
	Document val = new DocumentImpl(uri, "hello, goodbye".getBytes());
	
	DocumentPersistenceManager dpm = new DocumentPersistenceManager(baseDir);
	
	@Test
	public void test() throws IOException {
		dpm.serialize(uri, val);
		Document val1 = dpm.deserialize(uri);
		assertEquals(val1, val);
	}
	
	private File getFile(URI uri) {
    	return new File(baseDir.toString() + File.separatorChar +
    	((uri.getHost() != null ? uri.getHost() : "") +
    	(uri.getPath() != null ? uri.getPath() : "")) + ".json");
    }
}
