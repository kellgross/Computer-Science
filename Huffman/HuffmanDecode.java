import java.io.*;
import java.util.*;

public class HuffmanDecode {

	public HuffmanDecode(String in, String out) {
		//implements the huffman decoding algorithm
		//add private methods as needed
		
		HuffmanInputStream stream = new HuffmanInputStream(in);
		int totalChars = stream.totalChars();
		String huffmantree = stream.getTree();
		
		//make a new huffman tree
		HuffmanTree tree = new HuffmanTree(huffmantree, (char) 128);
		
		try {
			readFile(stream, tree, out);
		} catch(FileNotFoundException e) {
			System.out.println("FileNotFoundException");
		}
		
		
		
	}
	
	private void readFile(HuffmanInputStream in, HuffmanTree tree, String out) throws FileNotFoundException { //have to read bits backwards
		//MoveRoot
		//move right or left until you hit a leaf
		int charactercount = 0;
		PrintWriter output = new PrintWriter(out);
		while (charactercount < in.totalChars()) {
			tree.moveRoot();
			while (!tree.atLeaf()) {
				int bit = in.readBit();
				if (bit == 0) {
					tree.moveLeft();
				}
				else { //bit == 0
					tree.moveRight();
				}
			}
			output.print(tree.current());
			charactercount++;
			tree.moveRoot();
		}
			
		output.close();
	}
	
	public static void main(String args[]) {
		new HuffmanDecode(args[0], args[1]);
	}
	
	
	
}
