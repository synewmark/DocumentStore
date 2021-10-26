package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.stage3.Document;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;

import org.junit.jupiter.api.*;

public class DocumentImplTest {
    String[] stringArray = {"Standard String",
    		"String with double  spaces",
    		"$tr!ng with dro500 ch@#*,rs",
    		"String with trailing spaces  ",
    		"  String with leading spaces",
    		"?? String with dropped characters and spaces ??"};
    URI uri = URI.create("www.uri.com");
	@Test
	public void documentByteArrayTest() {
		
		Document document = new DocumentImpl(uri, stringArray[0].getBytes());
		assertEquals(document.getWords().size(), 0);
		assertEquals(document.wordCount("string"), 0);
	}
	@Test
	public void stringArrayTest() {
		Document[] docArray = new Document[6];
		for (int i = 0; i < 6; i++) {
			docArray[i] = new DocumentImpl(uri, stringArray[i]);
		}
		assertEquals(docArray[0].getWords().size(), 2);
		assertEquals(docArray[1].getWords().size(), 4);
		assertEquals(docArray[2].getWords().size(), 4);
		assertEquals(docArray[3].getWords().size(), 4);
		assertEquals(docArray[4].getWords().size(), 4);
		assertEquals(docArray[5].getWords().size(), 6);
	}

}
