package edu.yu.cs.com1320.project.impl;
import static org.junit.jupiter.api.Assertions.*;
import edu.yu.cs.com1320.project.HashTable;

import org.junit.jupiter.api.Test;
//presented without comment
class HashTableImplTest {
	@Test
	  public void hashTableImplSimplePutAndGet() {
	   HashTable<Integer,Integer> hashTable = new HashTableImpl<Integer,Integer>();
	   hashTable.put(1,2);
	   hashTable.put(3,6);
	   hashTable.put(7,14);
	   int x = hashTable.get(1);
	   int y = hashTable.get(3);
	   int z = hashTable.get(7);
	   assertEquals(2, x);
	   assertEquals(6, y);
	   assertEquals(14, z);
	   
	   
	    
	  }
	  
	  @Test
	  public void hashTableImplALotOfInfoTest() {
	   HashTable<Integer,Integer> hashTable = new HashTableImpl<Integer,Integer>();
	   for (int i = 0; i<1000; i++) {
	    hashTable.put(i,2*i);
	   }
	   
	   int aa = hashTable.get(450);
	   assertEquals(900, aa);
	  }
	  
	  
	  @Test
	  public void hashTableImplCollisionTest() {
	   HashTable<Integer,Integer> hashTable = new HashTableImpl<Integer,Integer>();
	   hashTable.put(1, 9);
	   hashTable.put(6,12);
	   hashTable.put(11,22);
	   int a = hashTable.get(1);
	   int b = hashTable.get(6);
	   int c = hashTable.get(11);
	   assertEquals(9, a);
	   assertEquals(12, b);
	   assertEquals(22, c);
	  }
	  
	  @Test
	  public void hashTableImplReplacementTest() {
	   HashTable<Integer,Integer> hashTable = new HashTableImpl<Integer,Integer>();
	   hashTable.put(1,2);
	   int a = hashTable.put(1, 3);
	   assertEquals(2, a);
	   int b = hashTable.put(1, 4);
	   assertEquals(3,b);
	   int c = hashTable.put(1, 9);
	   assertEquals(4, c);
	  }
	  @Test
	  public void hashTableDelNullPut() {
		  HashTable<String,Integer> hashTable = new HashTableImpl<String,Integer>();
		  
		  hashTable.put("Defied", (Integer)22345);
		  Integer test1a = hashTable.get("Defied");
		  assertEquals(test1a, (Integer)22345);
		  hashTable.put("Defied", null);
		  Integer test1b = hashTable.get("Defied");
		  assertEquals(test1b,null);
		  hashTable.put("Oakland", 87123);
		  
		  Integer test2a = hashTable.get("Oakland");
		  assertEquals(test2a, (Integer)87123);
		  hashTable.put("Oakland", null);
		  hashTable.get("Oakland");
		  Integer test2b = hashTable.get("Oakland");
		  assertEquals(test2b,null);
		  
		  hashTable.put("Sanguine", (Integer)4682);
		  Integer test3a = hashTable.get("Sanguine");
		  assertEquals(test3a, (Integer)4682);
		  hashTable.put("Sanguine", null);
		  Integer test3b = hashTable.get("Sanguine");
		  assertEquals(test3b,null);
	  }
	  
	  @Test
	  public void HashEqualButNotEqual() {
		  HashTable<String,Integer> hashTable = new HashTableImpl<String,Integer>();
		  
		  hashTable.put("tensada", 3521);
		  hashTable.put("friabili", 1253);
		  Integer test1a = hashTable.get("tensada");
		  assertEquals(test1a, (Integer)3521);
		  Integer test1b = hashTable.get("friabili");
		  assertEquals(test1b, (Integer)1253);
		  
		  hashTable.put("abyz", 8948);
		  hashTable.put("abzj", 84980);
		  Integer test2a = hashTable.get("abyz");
		  assertEquals(test2a, (Integer)8948);
		  Integer test2b = hashTable.get("abzj");
		  assertEquals(test2b, 84980);
		  
		  hashTable.put("Siblings", 27128);
		  hashTable.put("Teheran", 82172);
		  Integer test3a = hashTable.get("Siblings");
		  assertEquals(test3a, (Integer)27128);
		  Integer test3b = hashTable.get("Teheran");
		  assertEquals(test3b, (Integer)82172);
		  
	  }
}

