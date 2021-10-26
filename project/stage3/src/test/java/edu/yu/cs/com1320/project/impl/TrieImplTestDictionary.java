package edu.yu.cs.com1320.project.impl;


import java.io.*;
import java.util.Comparator;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import edu.yu.cs.com1320.project.Trie;


public class TrieImplTestDictionary {
	File file = new File("C:\\Users\\ahome\\Downloads\\words.txt");
	Trie<Integer> trie = new TrieImpl<>();
	int i = 1;
	char ch = 'a';
	@Test
	public void entireDictionary()  throws Exception {
		entireDictionary(0);
//		((TrieImpl<Integer>) trie).printEverything();
	}
	String sanitize (String string) {
		return string.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
	}
	
	public void entireDictionary(int n) throws FileNotFoundException {
		Scanner scanner1 = new Scanner(file);
		while (scanner1.hasNext()) {
			trie.put(sanitize(scanner1.nextLine()+ch), i++);
		}
		System.out.println(i+" "+ch);
		ch++;
		scanner1.close();
		entireDictionary(n);
	}
	
	@Test
	public void arrayGeneric() {
		Trie<int[]> trie = new TrieImpl<>();
	}
}