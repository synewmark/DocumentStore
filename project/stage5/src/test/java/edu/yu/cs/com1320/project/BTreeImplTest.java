package edu.yu.cs.com1320.project;

import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager;
import edu.yu.cs.com1320.project.stage5.Document;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

public class BTreeImplTest {
	URI[] uriArray = new URI[21];
	Document[] docArray = new Document[21];
	String[] stringArray = {"The blue parrot drove by the hitchhiking mongoose.",
		"She thought there'd be sufficient time if she hid her watch.",
		"Choosing to do nothing is still a choice, after all.",
		"He found the chocolate covered roaches quite tasty.",
		"The efficiency we have at removing trash has made creating trash more acceptable.",
		"Peanuts don't grow on trees, but cashews do.",
		"A song can make or ruin a person's day if they let it get to them.",
		"You bite up because of your lower jaw.",
		"He realized there had been several deaths on this road, but his concern rose when he saw the exact number.",
		"So long and thanks for the fish.",
		"Three years later, the coffin was still full of Jello.",
		"Weather is not trivial - it's especially important when you're standing in it.",
		"He walked into the basement with the horror movie from the night before playing in his head.",
		"He wondered if it could be called a beach if there was no sand.",
		"Jeanne wished she has chosen the red button.",
		"It's much more difficult to play tennis with a bowling ball than it is to bowl with a tennis ball.",
		"Pat ordered a ghost pepper pie.",
		"Everyone says they love nature until they realize how dangerous she can be.",
		"The memory we used to share is no longer coherent.",
		"My harvest will come Tiny valorous straw Among the millions Facing to the sun",
		"A dreamy-eyed child staring into night On a journey to storyteller's mind Whispers a wish speaks with the stars the words are silent in him"};

    private BTree<URI,Document> table;

    @BeforeEach
    public void initTable(){
    	for (int i = 0; i < 21; i++) {
    		try {
				uriArray[i] = new URI("www.google.com/file"+i+"/doc"+i);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
    		docArray[i] = new DocumentImpl(uriArray[i], stringArray[i]);
    	}
        this.table = new BTreeImpl<>();
        this.table.setPersistenceManager(new DocumentPersistenceManager(null));
        for (int i = 0; i < 21; i++) {
        	table.put(uriArray[i], docArray[i]);
        }
    }
    @Test
    public void testGet() {
        for (int i = 0; i < 21; i++) {
        	assertEquals(docArray[i], table.get(uriArray[i]));
        }
    }
    @Test
    public void testGetMiss() {
        assertEquals(null,this.table.get(URI.create("www.yu.edu")));
    }
    @Test
    public void testPutReturnValue() {
        assertEquals(docArray[10],this.table.put(uriArray[10],docArray[10]));
        assertEquals(docArray[15],this.table.put(uriArray[15],docArray[9]));
        assertEquals(docArray[20],this.table.put(uriArray[20],docArray[3]));
    }
    @Test
    public void testGetChangedValue () {
        assertEquals(docArray[10],this.table.put(uriArray[10],docArray[10]));
        assertEquals(docArray[15],this.table.put(uriArray[15],docArray[9]));
        assertEquals(docArray[20],this.table.put(uriArray[20],docArray[3]));
        
        assertEquals(docArray[10],table.get(uriArray[10]));
        assertEquals(docArray[9],table.get(uriArray[15]));
        assertEquals(docArray[3],table.get(uriArray[20]));
    }
    @Test
    public void testDeleteViaPutNull() {
        for (int i = 0; i < 21; i++) {
        	assertEquals(docArray[i],table.put(uriArray[i], null));
        }
        for (int i = 0; i < 21; i++) {
        	assertEquals(null, table.get(uriArray[i]));
        }
        for (int i = 0; i < 21; i++) {
        	assertEquals(null,table.put(uriArray[i], docArray[i]));
        }
        for (int i = 0; i < 21; i++) {
        	assertEquals(docArray[i],table.get(uriArray[i]));
        }
    }
    @Test
    public void testSerialize() throws Exception {
    	for (int i = 0; i < 21; i++) {
    		table.moveToDisk(uriArray[i]);
        }
    	assertEquals(docArray[0],table.get(uriArray[0]));
    	for (int i = 0; i < 21; i++) {
        	assertEquals(docArray[i],table.get(uriArray[i]));
        }
    }
}