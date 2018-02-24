import java.io.*; 
import java.util.*;


public class HuffmanEncode {
	
	int totalCharacters;
	
	public HuffmanEncode(String in, String out) {
		//implements the huffman encoding algorithm
		//add private methods as needed
		PriorityQueue<Item> characterQueue = new PriorityQueue<>(128);
		
		int[] characterArray = new int[128]; 
		characterArray = readFile(in, characterArray);
		
		//putting everything from array into priority queue
		for (int i = 0; i < characterArray.length; ++i) {
			if (characterArray[i] > 0) {
				HuffmanTree temp = new HuffmanTree((char) i);
				characterQueue.add(new Item(temp, characterArray[i]));
			}
		}
		
		//popping two things from priority queue, merging them, then pushing that
		//tree back onto the priority queue
		while (characterQueue.size() > 1) {
			char d = (char) 128;
			Item item1 = characterQueue.poll();
			Item item2 = characterQueue.poll();
			HuffmanTree newTree = new HuffmanTree(item1.tree, item2.tree, d);
			int newNum= item1.frequency + item2.frequency;
			characterQueue.add(new Item(newTree, newNum));
		}
		
		HuffmanTree tree = characterQueue.poll().tree;
		
		//Step 3
		String[] encodingsArray = new String[128];
		Iterator<String> iter = tree.iterator();
		while (iter.hasNext()) {
			String s = iter.next();
			char c = s.charAt(0);
			encodingsArray[(int) c] = s.substring(1, s.length());
		}

		//step 4: write representation of huffmantree to output file
		//step 5: write totalCharacters to output file
		HuffmanOutputStream outFile = new HuffmanOutputStream(out, tree.toString(), totalCharacters);
		
		
		//step 6: for each character in input file, write the bits to output file
		writeFile(in, outFile, encodingsArray);
		
		
		
	} 
	
	public static void main(String args[]) {
		new HuffmanEncode(args[0], args[1]);
	}
	
	

	private class Item implements Comparable { 
	    	HuffmanTree tree;
	    	int frequency;
	    	
	    	public Item(HuffmanTree t, int n) {
	    		tree = t;
	    		frequency = n;
	    		
	    	}
	    	
	    	public int compareTo(Object x) {
	    		return frequency - (((Item) x).frequency);
	    	}
	    }
		
	
	private int[] readFile(String file, int[] array) {
		//method to read in the characters from a file and find their frequencies
		try {
			FileReader fin = new FileReader(file);
			BufferedReader reader = new BufferedReader(fin);
			
			int character = reader.read();
			
			//while not at end of file add one to that character in the array
			while (character != -1) {
				array[character]++;	
				totalCharacters++;
				character = reader.read();
			}
		} catch (IOException e) {
			System.out.println("IOException");
		}
		return array;
	}

	private void writeFile(String in, HuffmanOutputStream out, String[] array) {
		
		try {
			FileReader fin = new FileReader(in);
			BufferedReader reader = new BufferedReader(fin);
			
			int character = reader.read();
			while (character != -1) {
				//write the bits to file
				for (int i = 0; i < array[character].length(); ++i) {
					String s = array[character];
					out.writeBit(s.charAt(i) - '0');
				}
				character = reader.read();
			}
			fin.close();
			reader.close();
			out.close();
		}
		catch (IOException e) {
			System.out.print("IOException");
		}
	}

}
