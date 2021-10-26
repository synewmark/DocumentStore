package edu.yu.cs.com1320.project.impl;

import java.util.List;
import java.util.Set;

import edu.yu.cs.com1320.project.Trie;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class TrieImplTest2 {
	@Test
	public void simpleTrieTest() {
	  Trie trie = new TrieImpl<Integer>();
	  trie.put("APPLE123", 1);
	  trie.put("APPLE123", 2);
	  trie.put("APPLE123", 3);
	  trie.put("WORD87", 8);
	  trie.put("WORD87", 7);

	  List<Integer> apple123List = trie.getAllSorted("apple123", (int1, int2) -> {
	   if ((int) int1 < (int) int2) {
	    return -1;
	   } else if ((int) int2 < (int) int1) {
	    return 1;
	   }
	   return 0;});//this comparator will order integers from lowest to highest
	  List<Integer> word87List = trie.getAllSorted("word87", (int1, int2) -> {
	   if ((int) int1 < (int) int2) {
	    return -1;
	   } else if ((int) int2 < (int) int1) {
	    return 1;
	   }
	   return 0;});

	  assertEquals(3, apple123List.size());
	  assertEquals(2, word87List.size());
	  assertEquals(1, apple123List.get(0));
	  assertEquals(2, apple123List.get(1));
	  assertEquals(3, apple123List.get(2));
	  assertEquals(7, word87List.get(0));
	  assertEquals(8, word87List.get(1));

	  trie.put("app", 12);
	  trie.put("app", 5);
	  trie.put("ap", 4);

	  List<Integer> apList = trie.getAllWithPrefixSorted("AP", (int1, int2) -> {
	   if ((int) int1 < (int) int2) {
	    return -1;
	   } else if ((int) int2 < (int) int1) {
	    return 1;
	   }
	   return 0;});
	  List<Integer> appList = trie.getAllWithPrefixSorted("APP", (int1, int2) -> {
	   if ((int) int1 < (int) int2) {
	    return -1;
	   } else if ((int) int2 < (int) int1) {
	    return 1;
	   }
	   return 0;});

	  assertEquals(6, apList.size());
	  assertEquals(5, appList.size());
	  assertEquals(12, apList.get(5));
	  assertEquals(12, appList.get(4));

	  Set<Integer> deletedAppPrefix = trie.deleteAllWithPrefix("aPp");
	  assertEquals(5, deletedAppPrefix.size());
	  assertTrue(deletedAppPrefix.contains(3));
	  assertTrue(deletedAppPrefix.contains(5));

	  apList = trie.getAllWithPrefixSorted("AP", (int1, int2) -> {
	   if ((int) int1 < (int) int2) {
	    return -1;
	   } else if ((int) int2 < (int) int1) {
	    return 1;
	   }
	   return 0;});
	  appList = trie.getAllWithPrefixSorted("APP", (int1, int2) -> {
	   if ((int) int1 < (int) int2) {
	    return -1;
	   } else if ((int) int2 < (int) int1) {
	    return 1;
	   }
	   return 0;});

	  assertEquals(1, apList.size());
	  assertEquals(0, appList.size());

	  trie.put("deleteAll", 100);
	  trie.put("deleteAll", 200);
	  trie.put("deleteAll", 300);

	  List<Integer> deleteList = trie.getAllSorted("DELETEALL", (int1, int2) -> {
	   if ((int) int1 < (int) int2) {
	    return -1;
	   } else if ((int) int2 < (int) int1) {
	    return 1;
	   }
	   return 0;});

	  assertEquals(3, deleteList.size());
	  Set<Integer> thingsActuallyDeleted = trie.deleteAll("DELETEall");
	  assertEquals(3, thingsActuallyDeleted.size());
	  assertTrue(thingsActuallyDeleted.contains(100));

	  deleteList = trie.getAllSorted("DELETEALL", (int1, int2) -> {
	   if ((int) int1 < (int) int2) {
	    return -1;
	   } else if ((int) int2 < (int) int1) {
	    return 1;
	   }
	   return 0;});

	  assertEquals(0, deleteList.size());

	  trie.put("deleteSome", 100);
	  trie.put("deleteSome", 200);
	  trie.put("deleteSome", 300);

	  List<Integer> deleteList2 = trie.getAllSorted("DELETESOME", (int1, int2) -> {
	   if ((int) int1 < (int) int2) {
	    return -1;
	   } else if ((int) int2 < (int) int1) {
	    return 1;
	   }
	   return 0;});

	  assertEquals(3, deleteList2.size());
	  Integer twoHundred = (Integer) trie.delete("deleteSome", 200);
	  Integer nullInt = (Integer) trie.delete("deleteSome", 500);

	  assertEquals(200, twoHundred);
	  assertNull(nullInt);

	  deleteList2 = trie.getAllSorted("DELETESOME", (int1, int2) -> {
	   if ((int) int1 < (int) int2) {
	    return -1;
	   } else if ((int) int2 < (int) int1) {
	    return 1;
	   }
	   return 0;});

	  assertEquals(2, deleteList2.size());
	  assertFalse(deleteList2.contains(200));
	 }
}
