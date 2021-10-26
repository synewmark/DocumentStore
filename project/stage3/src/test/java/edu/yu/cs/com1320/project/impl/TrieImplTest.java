package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.Test;

public class TrieImplTest {
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
			"Weather is not trivial it's especially important when you're standing in it.",
			"He walked into the basement with the horror movie from the night before playing in his head.",
			"He wondered if it could be called a beach if there was no sand.",
			"Jeanne wished she has chosen the red button.",
			"It's much more difficult to play tennis with a bowling ball than it is to bowl with a tennis ball.",
			"Pat ordered a ghost pepper pie.",
			"Everyone says they love nature until they realize how dangerous she can be.",
			"The memory we used to share is no longer coherent.",
			"My harvest will come Tiny valorous straw Among the millions Facing to the sun",
			"A dreamy-eyed child staring into night On a journey to storyteller's mind Whispers a wish speaks with the stars the words are silent in him"};
	
	@Test
	void testPutAndGet() {
		Set<Integer> theset = new HashSet<>();
		Map<String, Set<Integer>> map = new HashMap<>();
		Trie<Integer> trie = new TrieImpl<Integer>();
		for (int i = 0; i < stringArray.length; i++) {
			String[] words = stringArray[i].split(" ");
			for (int j = 0; j < words.length; j++) {
				if (sanitize(words[j]).startsWith("the")) {
					theset.add(i+j);
				}
				
				trie.put(sanitize(words[j]), i+j);
				Set<Integer> set = map.get(sanitize(words[j]));
				if (set == null) {
					set = new HashSet<>();
					map.put(sanitize(words[j]), set);
				}
				set.add(i+j);
			}
		}
		
		
		for (int i = 0; i < stringArray.length; i++) {
			String[] words = stringArray[i].split(" ");
			for (int j = 0; j < words.length; j++) {
//				System.out.println(trie.getAllSorted(words[j], new Comparator<Integer>() {
//					@Override
//					public int compare(Integer o1, Integer o2) {
//						return o2 - o1;
//					}
//				}));
//				System.out.println(sanitize(words[j]));
				List<Integer> list = new ArrayList<Integer>(map.get(sanitize(words[j])));
				Collections.sort(list);
				assertEquals(trie.getAllSorted(sanitize(words[j]), new Comparator<Integer>() {
					@Override
					public int compare(Integer o1, Integer o2) {
						return o1 - o2;
					}
				}), list);
			}
		}
		List<Integer> thelist = new ArrayList<>(theset);
		Collections.sort(thelist);
		assertEquals(trie.getAllWithPrefixSorted("the", new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o1 - o2;
			}
		}), thelist);
	}
	String sanitize (String string) {
		return string.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
	}
}
